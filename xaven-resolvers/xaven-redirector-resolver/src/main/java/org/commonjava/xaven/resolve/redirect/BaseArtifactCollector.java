package org.commonjava.xaven.resolve.redirect;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.CyclicDependencyException;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.ManagedVersionMap;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Based on Default implementation of the artifact collector from 2.2.1.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: DefaultArtifactCollector.java 801437 2009-08-05 22:06:54Z jdcasey $
 */
public class BaseArtifactCollector
    implements ArtifactCollector
{
    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult collect( final Set artifacts, final Artifact originatingArtifact,
                                             final ArtifactRepository localRepository, final List remoteRepositories,
                                             final ArtifactMetadataSource source, final ArtifactFilter filter,
                                             final List listeners )
        throws ArtifactResolutionException
    {
        return collect( artifacts, originatingArtifact, Collections.EMPTY_MAP, localRepository, remoteRepositories,
                        source, filter, listeners );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult collect( final Set artifacts, final Artifact originatingArtifact,
                                             final Map managedVersions, final ArtifactRepository localRepository,
                                             final List remoteRepositories, final ArtifactMetadataSource source,
                                             final ArtifactFilter filter, final List listeners )
        throws ArtifactResolutionException
    {
        final Map<String, List<ResolutionNode>> resolvedArtifacts = new LinkedHashMap<String, List<ResolutionNode>>();

        final ResolutionNode root = new ResolutionNode( originatingArtifact, remoteRepositories );

        root.addDependencies( artifacts, remoteRepositories, filter );

        final ManagedVersionMap versionMap = getManagedVersionsMap( originatingArtifact, managedVersions );

        recurse( originatingArtifact, root, resolvedArtifacts, versionMap, localRepository, remoteRepositories, source,
                 filter, listeners );

        final Set<ResolutionNode> set = new LinkedHashSet<ResolutionNode>();

        for ( final List<ResolutionNode> nodes : resolvedArtifacts.values() )
        {
            for ( final ResolutionNode node : nodes )
            {
                if ( !node.equals( root ) && node.isActive() )
                {
                    final Artifact artifact = node.getArtifact();

                    if ( node.filterTrail( filter ) )
                    {
                        // If it was optional and not a direct dependency,
                        // we don't add it or its children, just allow the update of the version and scope
                        if ( node.isChildOfRootNode() || !artifact.isOptional() )
                        {
                            artifact.setDependencyTrail( node.getDependencyTrail() );

                            set.add( node );
                        }
                    }
                }
            }
        }

        final ArtifactResolutionResult result = new ArtifactResolutionResult();
        result.setArtifactResolutionNodes( set );
        return result;
    }

    /**
     * Get the map of managed versions, removing the originating artifact if it is also in managed versions
     * @param originatingArtifact artifact we are processing
     * @param managedVersions original managed versions
     */
    protected ManagedVersionMap getManagedVersionsMap( final Artifact originatingArtifact,
                                                       final Map<String, Artifact> managedVersions )
    {
        ManagedVersionMap versionMap;
        if ( managedVersions != null && managedVersions instanceof ManagedVersionMap )
        {
            versionMap = (ManagedVersionMap) managedVersions;
        }
        else
        {
            versionMap = new ManagedVersionMap( managedVersions );
        }

        /* remove the originating artifact if it is also in managed versions to avoid being modified during resolution */
        final Artifact managedOriginatingArtifact =
            (Artifact) versionMap.get( originatingArtifact.getDependencyConflictId() );
        if ( managedOriginatingArtifact != null )
        {
            // TODO we probably want to warn the user that he is building an artifact with
            // different values than in dependencyManagement
            if ( managedVersions instanceof ManagedVersionMap )
            {
                /* avoid modifying the managedVersions parameter creating a new map */
                versionMap = new ManagedVersionMap( managedVersions );
            }
            versionMap.remove( originatingArtifact.getDependencyConflictId() );
        }

        return versionMap;
    }

    @SuppressWarnings( "unchecked" )
    protected void recurse( final Artifact originatingArtifact, final ResolutionNode node,
                            final Map<String, List<ResolutionNode>> resolvedArtifacts,
                            final ManagedVersionMap managedVersions, final ArtifactRepository localRepository,
                            final List<ArtifactRepository> remoteRepositories, final ArtifactMetadataSource source,
                            final ArtifactFilter filter, final List<ResolutionListener> listeners )
        throws CyclicDependencyException, ArtifactResolutionException, OverConstrainedVersionException
    {
        fireEvent( ResolutionListener.TEST_ARTIFACT, listeners, node );

        final String key = node.getKey().toString();

        // TODO: Does this check need to happen here?  Had to add the same call
        // below when we iterate on child nodes -- will that suffice?
        if ( managedVersions.containsKey( key ) )
        {
            manageArtifact( node, managedVersions, listeners );
        }

        List<ResolutionNode> previousNodes = resolvedArtifacts.get( key );
        if ( previousNodes != null )
        {
            for ( final ResolutionNode previous : previousNodes )
            {
                if ( previous.isActive() )
                {
                    // Version mediation
                    final VersionRange previousRange = previous.getArtifact().getVersionRange();
                    final VersionRange currentRange = node.getArtifact().getVersionRange();

                    if ( previousRange != null && currentRange != null )
                    {
                        // TODO: shouldn't need to double up on this work, only done for simplicity of handling recommended
                        // version but the restriction is identical
                        final VersionRange newRange = previousRange.restrict( currentRange );
                        // TODO: ick. this forces the OCE that should have come from the previous call. It is still correct
                        if ( newRange.isSelectedVersionKnown( previous.getArtifact() ) )
                        {
                            fireEvent( ResolutionListener.RESTRICT_RANGE, listeners, node, previous.getArtifact(),
                                       newRange );
                        }
                        previous.getArtifact().setVersionRange( newRange );
                        node.getArtifact().setVersionRange( currentRange.restrict( previousRange ) );

                        //Select an appropriate available version from the (now restricted) range
                        //Note this version was selected before to get the appropriate POM
                        //But it was reset by the call to setVersionRange on restricting the version
                        final ResolutionNode[] resetNodes = { previous, node };
                        for ( int j = 0; j < 2; j++ )
                        {
                            final Artifact resetArtifact = resetNodes[j].getArtifact();

                            //MNG-2123: if the previous node was not a range, then it wouldn't have any available
                            //versions. We just clobbered the selected version above. (why? i have no idea.)
                            //So since we are here and this is ranges we must go figure out the version (for a third time...)
                            if ( resetArtifact.getVersion() == null && resetArtifact.getVersionRange() != null )
                            {

                                // go find the version. This is a total hack. See previous comment.
                                List<ArtifactVersion> versions = resetArtifact.getAvailableVersions();
                                if ( versions == null )
                                {
                                    try
                                    {
                                        versions =
                                            source.retrieveAvailableVersions( resetArtifact, localRepository,
                                                                              remoteRepositories );
                                        resetArtifact.setAvailableVersions( versions );
                                    }
                                    catch ( final ArtifactMetadataRetrievalException e )
                                    {
                                        resetArtifact.setDependencyTrail( node.getDependencyTrail() );
                                        throw new ResolutionException( "Unable to get dependency information: "
                                            + e.getMessage(), resetArtifact, remoteRepositories, e );
                                    }
                                }
                                //end hack

                                //MNG-2861: match version can return null
                                final ArtifactVersion selectedVersion =
                                    resetArtifact.getVersionRange().matchVersion( resetArtifact.getAvailableVersions() );
                                if ( selectedVersion != null )
                                {
                                    resetArtifact.selectVersion( selectedVersion.toString() );
                                }
                                else
                                {
                                    throw new OverConstrainedVersionException( " Unable to find a version in "
                                        + resetArtifact.getAvailableVersions() + " to match the range "
                                        + resetArtifact.getVersionRange(), resetArtifact );
                                }

                                fireEvent( ResolutionListener.SELECT_VERSION_FROM_RANGE, listeners, resetNodes[j] );
                            }
                        }
                    }

                    // Conflict Resolution
                    // TODO: use as conflict resolver(s), chain

                    // TODO: should this be part of mediation?
                    // previous one is more dominant
                    ResolutionNode nearest;
                    ResolutionNode farthest;
                    if ( previous.getDepth() <= node.getDepth() )
                    {
                        nearest = previous;
                        farthest = node;
                    }
                    else
                    {
                        nearest = node;
                        farthest = previous;
                    }

                    if ( checkScopeUpdate( farthest, nearest, listeners ) )
                    {
                        // if we need to update scope of nearest to use farthest scope, use the nearest version, but farthest scope
                        nearest.disable();
                        farthest.getArtifact().setVersion( nearest.getArtifact().getVersion() );
                        fireEvent( ResolutionListener.OMIT_FOR_NEARER, listeners, nearest, farthest.getArtifact() );
                    }
                    else
                    {
                        farthest.disable();
                        fireEvent( ResolutionListener.OMIT_FOR_NEARER, listeners, farthest, nearest.getArtifact() );
                    }
                }
            }
        }
        else
        {
            previousNodes = new ArrayList<ResolutionNode>();
            resolvedArtifacts.put( key, previousNodes );
        }
        previousNodes.add( node );

        if ( node.isActive() )
        {
            fireEvent( ResolutionListener.INCLUDE_ARTIFACT, listeners, node );
        }

        // don't pull in the transitive deps of a system-scoped dependency.
        if ( node.isActive() && !Artifact.SCOPE_SYSTEM.equals( node.getArtifact().getScope() ) )
        {
            fireEvent( ResolutionListener.PROCESS_CHILDREN, listeners, node );

            final Artifact parentArtifact = node.getArtifact();

            for ( final Iterator i = node.getChildrenIterator(); i.hasNext(); )
            {
                final ResolutionNode child = (ResolutionNode) i.next();

                // We leave in optional ones, but don't pick up its dependencies
                if ( !child.isResolved() && ( !child.getArtifact().isOptional() || child.isChildOfRootNode() ) )
                {
                    Artifact artifact = child.getArtifact();
                    artifact.setDependencyTrail( node.getDependencyTrail() );

                    final List<ArtifactRepository> childRemoteRepositories = child.getRemoteRepositories();
                    try
                    {
                        Object childKey;
                        do
                        {
                            childKey = child.getKey();

                            if ( managedVersions.containsKey( childKey ) )
                            {
                                // If this child node is a managed dependency, ensure
                                // we are using the dependency management version
                                // of this child if applicable b/c we want to use the
                                // managed version's POM, *not* any other version's POM.
                                // We retrieve the POM below in the retrieval step.
                                manageArtifact( child, managedVersions, listeners );

                                // Also, we need to ensure that any exclusions it presents are
                                // added to the artifact before we retrieve the metadata
                                // for the artifact; otherwise we may end up with unwanted
                                // dependencies.
                                final Artifact ma = (Artifact) managedVersions.get( childKey );
                                final ArtifactFilter managedExclusionFilter = ma.getDependencyFilter();
                                if ( null != managedExclusionFilter )
                                {
                                    if ( null != artifact.getDependencyFilter() )
                                    {
                                        final AndArtifactFilter aaf = new AndArtifactFilter();
                                        aaf.add( artifact.getDependencyFilter() );
                                        aaf.add( managedExclusionFilter );
                                        artifact.setDependencyFilter( aaf );
                                    }
                                    else
                                    {
                                        artifact.setDependencyFilter( managedExclusionFilter );
                                    }
                                }
                            }

                            if ( artifact.getVersion() == null )
                            {
                                // set the recommended version
                                // TODO: maybe its better to just pass the range through to retrieval and use a transformation?
                                ArtifactVersion version;
                                if ( artifact.isSelectedVersionKnown() )
                                {
                                    version = artifact.getSelectedVersion();
                                }
                                else
                                {
                                    //go find the version
                                    List<ArtifactVersion> versions = artifact.getAvailableVersions();
                                    if ( versions == null )
                                    {
                                        versions =
                                            source.retrieveAvailableVersions( artifact, localRepository,
                                                                              childRemoteRepositories );
                                        artifact.setAvailableVersions( versions );
                                    }

                                    Collections.sort( versions );

                                    final VersionRange versionRange = artifact.getVersionRange();

                                    version = versionRange.matchVersion( versions );

                                    if ( version == null )
                                    {
                                        if ( versions.isEmpty() )
                                        {
                                            throw new OverConstrainedVersionException(
                                                                                       "No versions are present in the repository for the artifact with a range "
                                                                                           + versionRange, artifact,
                                                                                       childRemoteRepositories );
                                        }

                                        throw new OverConstrainedVersionException( "Couldn't find a version in "
                                            + versions + " to match range " + versionRange, artifact,
                                                                                   childRemoteRepositories );
                                    }
                                }

                                //this is dangerous because artifact.getSelectedVersion() can
                                //return null. However it is ok here because we first check if the
                                //selected version is known. As currently coded we can't get a null here.
                                artifact.selectVersion( version.toString() );
                                fireEvent( ResolutionListener.SELECT_VERSION_FROM_RANGE, listeners, child );
                            }

                            final Artifact relocated =
                                source.retrieveRelocatedArtifact( artifact, localRepository, childRemoteRepositories );
                            if ( relocated != null && !artifact.equals( relocated ) )
                            {
                                relocated.setDependencyFilter( artifact.getDependencyFilter() );
                                artifact = relocated;
                                child.setArtifact( artifact );
                            }
                        }
                        while ( !childKey.equals( child.getKey() ) );

                        if ( parentArtifact != null && parentArtifact.getDependencyFilter() != null
                            && !parentArtifact.getDependencyFilter().include( artifact ) )
                        {
                            // MNG-3769: the [probably relocated] artifact is excluded. 
                            // We could process exclusions on relocated artifact details in the
                            // MavenMetadataSource.createArtifacts(..) step, BUT that would
                            // require resolving the POM from the repository very early on in
                            // the build.
                            continue;
                        }

                        final ResolutionGroup rGroup =
                            source.retrieve( artifact, localRepository, childRemoteRepositories );

                        //TODO might be better to have source.retrieve() throw a specific exception for this situation
                        //and catch here rather than have it return null
                        if ( rGroup == null )
                        {
                            //relocated dependency artifact is declared excluded, no need to add and recurse further
                            continue;
                        }

                        child.addDependencies( rGroup.getArtifacts(), rGroup.getResolutionRepositories(), filter );

                    }
                    catch ( final CyclicDependencyException e )
                    {
                        // would like to throw this, but we have crappy stuff in the repo

                        fireEvent( ResolutionListener.OMIT_FOR_CYCLE, listeners,
                                   new ResolutionNode( e.getArtifact(), childRemoteRepositories, child ) );
                    }
                    catch ( final ArtifactMetadataRetrievalException e )
                    {
                        artifact.setDependencyTrail( node.getDependencyTrail() );
                        throw new ResolutionException( "Unable to get dependency information: " + e.getMessage(),
                                                       artifact, childRemoteRepositories, e );
                    }

                    recurse( originatingArtifact, child, resolvedArtifacts, managedVersions, localRepository,
                             childRemoteRepositories, source, filter, listeners );
                }
            }

            fireEvent( ResolutionListener.FINISH_PROCESSING_CHILDREN, listeners, node );
        }
    }

    protected void manageArtifact( final ResolutionNode node, final ManagedVersionMap managedVersions,
                                   final List<ResolutionListener> listeners )
    {
        final Artifact artifact = (Artifact) managedVersions.get( node.getKey() );

        // Before we update the version of the artifact, we need to know
        // whether we are working on a transitive dependency or not.  This
        // allows depMgmt to always override transitive dependencies, while
        // explicit child override depMgmt (viz. depMgmt should only
        // provide defaults to children, but should override transitives).
        // We can do this by calling isChildOfRootNode on the current node.

        if ( artifact.getVersion() != null
            && ( node.isChildOfRootNode() ? node.getArtifact().getVersion() == null : true ) )
        {
            fireEvent( ResolutionListener.MANAGE_ARTIFACT_VERSION, listeners, node, artifact );
            node.getArtifact().setVersion( artifact.getVersion() );
        }

        if ( artifact.getScope() != null && ( node.isChildOfRootNode() ? node.getArtifact().getScope() == null : true ) )
        {
            fireEvent( ResolutionListener.MANAGE_ARTIFACT_SCOPE, listeners, node, artifact );
            node.getArtifact().setScope( artifact.getScope() );
        }
    }

    /**
     * Check if the scope needs to be updated.
     * <a href="http://docs.codehaus.org/x/IGU#DependencyMediationandConflictResolution-Scoperesolution">More info</a>.
     *
     * @param farthest  farthest resolution node
     * @param nearest   nearest resolution node
     * @param listeners
     */
    protected boolean checkScopeUpdate( final ResolutionNode farthest, final ResolutionNode nearest,
                                        final List<ResolutionListener> listeners )
    {
        boolean updateScope = false;
        final Artifact farthestArtifact = farthest.getArtifact();
        final Artifact nearestArtifact = nearest.getArtifact();

        /* farthest is runtime and nearest has lower priority, change to runtime */
        if ( Artifact.SCOPE_RUNTIME.equals( farthestArtifact.getScope() )
            && ( Artifact.SCOPE_TEST.equals( nearestArtifact.getScope() ) || Artifact.SCOPE_PROVIDED.equals( nearestArtifact.getScope() ) ) )
        {
            updateScope = true;
        }

        /* farthest is compile and nearest is not (has lower priority), change to compile */
        if ( Artifact.SCOPE_COMPILE.equals( farthestArtifact.getScope() )
            && !Artifact.SCOPE_COMPILE.equals( nearestArtifact.getScope() ) )
        {
            updateScope = true;
        }

        /* current POM rules all, if nearest is in current pom, do not update its scope */
        if ( nearest.getDepth() < 2 && updateScope )
        {
            updateScope = false;

            fireEvent( ResolutionListener.UPDATE_SCOPE_CURRENT_POM, listeners, nearest, farthestArtifact );
        }

        if ( updateScope )
        {
            fireEvent( ResolutionListener.UPDATE_SCOPE, listeners, nearest, farthestArtifact );

            // previously we cloned the artifact, but it is more effecient to just update the scope
            // if problems are later discovered that the original object needs its original scope value, cloning may
            // again be appropriate
            nearestArtifact.setScope( farthestArtifact.getScope() );
        }

        return updateScope;
    }

    protected void fireEvent( final int event, final List<ResolutionListener> listeners, final ResolutionNode node )
    {
        fireEvent( event, listeners, node, null );
    }

    protected void fireEvent( final int event, final List<ResolutionListener> listeners, final ResolutionNode node,
                              final Artifact replacement )
    {
        fireEvent( event, listeners, node, replacement, null );
    }

    protected void fireEvent( final int event, final List<ResolutionListener> listeners, final ResolutionNode node,
                              final Artifact replacement, final VersionRange newRange )
    {
        for ( final ResolutionListener listener : listeners )
        {
            switch ( event )
            {
                case ResolutionListener.TEST_ARTIFACT:
                    listener.testArtifact( node.getArtifact() );
                    break;
                case ResolutionListener.PROCESS_CHILDREN:
                    listener.startProcessChildren( node.getArtifact() );
                    break;
                case ResolutionListener.FINISH_PROCESSING_CHILDREN:
                    listener.endProcessChildren( node.getArtifact() );
                    break;
                case ResolutionListener.INCLUDE_ARTIFACT:
                    listener.includeArtifact( node.getArtifact() );
                    break;
                case ResolutionListener.OMIT_FOR_NEARER:
                    listener.omitForNearer( node.getArtifact(), replacement );
                    break;
                case ResolutionListener.OMIT_FOR_CYCLE:
                    listener.omitForCycle( node.getArtifact() );
                    break;
                case ResolutionListener.UPDATE_SCOPE:
                    listener.updateScope( node.getArtifact(), replacement.getScope() );
                    break;
                case ResolutionListener.UPDATE_SCOPE_CURRENT_POM:
                    listener.updateScopeCurrentPom( node.getArtifact(), replacement.getScope() );
                    break;
                case ResolutionListener.SELECT_VERSION_FROM_RANGE:
                    listener.selectVersionFromRange( node.getArtifact() );
                    break;
                case ResolutionListener.RESTRICT_RANGE:
                    if ( node.getArtifact().getVersionRange().hasRestrictions()
                        || replacement.getVersionRange().hasRestrictions() )
                    {
                        listener.restrictRange( node.getArtifact(), replacement, newRange );
                    }
                    break;
                default:
                    throw new IllegalStateException( "Unknown event: " + event );
            }
        }
    }

}
