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
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DebugResolutionListener;
import org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.ResolutionNode;
import org.apache.maven.artifact.resolver.WarningResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.transform.ArtifactTransformationManager;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Based on DefaultArtifactResolver from 2.2.1
 */
public class BaseArtifactResolver
    extends AbstractLogEnabled
    implements ArtifactResolver
{
    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    private static final int DEFAULT_POOL_SIZE = 5;

    private WagonManager wagonManager;

    private ArtifactTransformationManager transformationManager;

    protected ArtifactFactory artifactFactory;

    private ArtifactCollector artifactCollector;

    private final ThreadPoolExecutor resolveArtifactPool;

    public BaseArtifactResolver()
    {
        resolveArtifactPool =
            new ThreadPoolExecutor( DEFAULT_POOL_SIZE, DEFAULT_POOL_SIZE, 3, TimeUnit.SECONDS,
                                    new LinkedBlockingQueue() );
    }

    // ----------------------------------------------------------------------
    // Implementation
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public void resolve( final Artifact artifact, final List remoteRepositories,
                         final ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        resolve( artifact, remoteRepositories, localRepository, false );
    }

    @SuppressWarnings( "unchecked" )
    public void resolveAlways( final Artifact artifact, final List remoteRepositories,
                               final ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        resolve( artifact, remoteRepositories, localRepository, true );
    }

    protected void resolve( final Artifact artifact, final List<ArtifactRepository> remoteRepositories,
                            final ArtifactRepository localRepository, boolean force )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        if ( artifact == null )
        {
            return;
        }

        if ( Artifact.SCOPE_SYSTEM.equals( artifact.getScope() ) )
        {
            final File systemFile = artifact.getFile();

            if ( systemFile == null )
            {
                throw new ArtifactNotFoundException( "System artifact: " + artifact + " has no file attached", artifact );
            }

            if ( !systemFile.isFile() )
            {
                throw new ArtifactNotFoundException( "System artifact: " + artifact + " is not a file: " + systemFile,
                                                     artifact );
            }

            if ( !systemFile.exists() )
            {
                throw new ArtifactNotFoundException( "System artifact: " + artifact + " not found in path: "
                    + systemFile, artifact );
            }

            artifact.setResolved( true );
        }
        else if ( !artifact.isResolved() )
        {
            // ----------------------------------------------------------------------
            // Check for the existence of the artifact in the specified local
            // ArtifactRepository. If it is present then simply return as the
            // request for resolution has been satisfied.
            // ----------------------------------------------------------------------

            final String localPath = localRepository.pathOf( artifact );

            artifact.setFile( new File( localRepository.getBasedir(), localPath ) );

            transformationManager.transformForResolve( artifact, remoteRepositories, localRepository );

            boolean localCopy = false;
            for ( final ArtifactMetadata m : artifact.getMetadataList() )
            {
                if ( m instanceof SnapshotArtifactRepositoryMetadata )
                {
                    final SnapshotArtifactRepositoryMetadata snapshotMetadata = (SnapshotArtifactRepositoryMetadata) m;

                    final Metadata metadata = snapshotMetadata.getMetadata();
                    if ( metadata != null )
                    {
                        final Versioning versioning = metadata.getVersioning();
                        if ( versioning != null )
                        {
                            final Snapshot snapshot = versioning.getSnapshot();
                            if ( snapshot != null )
                            {
                                localCopy = snapshot.isLocalCopy();
                            }
                        }
                    }
                }
            }

            final File destination = artifact.getFile();
            List<ArtifactRepository> repositories = remoteRepositories;

            // TODO: would prefer the snapshot transformation took care of this. Maybe we need a "shouldresolve" flag.
            if ( artifact.isSnapshot() && artifact.getBaseVersion().equals( artifact.getVersion() )
                && destination.exists() && !localCopy && wagonManager.isOnline() )
            {
                final Date comparisonDate = new Date( destination.lastModified() );

                // cull to list of repositories that would like an update
                repositories = new ArrayList<ArtifactRepository>( remoteRepositories );
                for ( final Iterator<ArtifactRepository> i = repositories.iterator(); i.hasNext(); )
                {
                    final ArtifactRepository repository = i.next();
                    final ArtifactRepositoryPolicy policy = repository.getSnapshots();
                    if ( !policy.isEnabled() || !policy.checkOutOfDate( comparisonDate ) )
                    {
                        i.remove();
                    }
                }

                if ( !repositories.isEmpty() )
                {
                    // someone wants to check for updates
                    force = true;
                }
            }

            if ( !destination.exists() || force )
            {
                if ( !wagonManager.isOnline() )
                {
                    throw new ArtifactNotFoundException( "System is offline.", artifact );
                }

                try
                {
                    // TODO: force should be passed to the wagon manager
                    if ( artifact.getRepository() != null )
                    {
                        // the transformations discovered the artifact - so use it exclusively
                        wagonManager.getArtifact( artifact, artifact.getRepository() );
                    }
                    else
                    {
                        wagonManager.getArtifact( artifact, repositories );
                    }

                    if ( !artifact.isResolved() && !destination.exists() )
                    {
                        throw new ArtifactResolutionException(
                                                               "Failed to resolve artifact, possibly due to a repository list that is not appropriately equipped for this artifact's metadata.",
                                                               artifact, getMirroredRepositories( remoteRepositories ) );
                    }
                }
                catch ( final ResourceDoesNotExistException e )
                {
                    throw new NotFoundException( e.getMessage(), artifact,
                                                 getMirroredRepositories( remoteRepositories ), e );
                }
                catch ( final TransferFailedException e )
                {
                    throw new ResolutionException( e.getMessage(), artifact,
                                                   getMirroredRepositories( remoteRepositories ), e );
                }
            }
            else if ( destination.exists() )
            {
                // locally resolved...no need to hit the remote repo.
                artifact.setResolved( true );
            }

            if ( artifact.isSnapshot() && !artifact.getBaseVersion().equals( artifact.getVersion() ) )
            {
                final String version = artifact.getVersion();
                artifact.selectVersion( artifact.getBaseVersion() );
                final File copy = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
                if ( !copy.exists() || copy.lastModified() != destination.lastModified()
                    || copy.length() != destination.length() )
                {
                    // recopy file if it was reresolved, or doesn't exist.
                    try
                    {
                        FileUtils.copyFile( destination, copy );
                        copy.setLastModified( destination.lastModified() );
                    }
                    catch ( final IOException e )
                    {
                        throw new ResolutionException( "Unable to copy resolved artifact for local use: "
                            + e.getMessage(), artifact, getMirroredRepositories( remoteRepositories ), e );
                    }
                }
                artifact.setFile( copy );
                artifact.selectVersion( version );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final ArtifactRepository localRepository,
                                                         final List remoteRepositories,
                                                         final ArtifactMetadataSource source,
                                                         final ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, Collections.EMPTY_MAP, localRepository,
                                    remoteRepositories, source, filter );

    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final Map managedVersions,
                                                         final ArtifactRepository localRepository,
                                                         final List remoteRepositories,
                                                         final ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, managedVersions, localRepository,
                                    remoteRepositories, source, null );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final Map managedVersions,
                                                         final ArtifactRepository localRepository,
                                                         final List remoteRepositories,
                                                         final ArtifactMetadataSource source,
                                                         final ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        // TODO: this is simplistic
        final List listeners = new ArrayList();
        if ( getLogger().isDebugEnabled() )
        {
            listeners.add( new DebugResolutionListener( getLogger() ) );
        }

        listeners.add( new WarningResolutionListener( getLogger() ) );

        return resolveTransitively( artifacts, originatingArtifact, managedVersions, localRepository,
                                    remoteRepositories, source, filter, listeners );

    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final Map managedVersions,
                                                         final ArtifactRepository localRepository,
                                                         final List remoteRepositories,
                                                         final ArtifactMetadataSource source,
                                                         final ArtifactFilter filter, final List listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitiveInternal( artifacts, originatingArtifact, managedVersions, localRepository,
                                          remoteRepositories, source, filter, listeners );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitiveInternal( final Set<Artifact> artifacts,
                                                               final Artifact originatingArtifact,
                                                               final Map<String, Artifact> managedVersions,
                                                               final ArtifactRepository localRepository,
                                                               final List<ArtifactRepository> remoteRepositories,
                                                               final ArtifactMetadataSource source,
                                                               final ArtifactFilter filter,
                                                               final List<ResolutionListener> listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        ArtifactResolutionResult artifactResolutionResult;
        artifactResolutionResult =
            artifactCollector.collect( artifacts, originatingArtifact, managedVersions, localRepository,
                                       remoteRepositories, source, filter, listeners );

        final List<Artifact> resolvedArtifacts = Collections.synchronizedList( new ArrayList<Artifact>() );
        final List<Artifact> missingArtifacts = Collections.synchronizedList( new ArrayList<Artifact>() );
        final CountDownLatch latch = new CountDownLatch( artifactResolutionResult.getArtifactResolutionNodes().size() );
        final Map<String, List<ResolutionNode>> nodesByGroupId = new HashMap<String, List<ResolutionNode>>();
        for ( final Iterator<ResolutionNode> i = artifactResolutionResult.getArtifactResolutionNodes().iterator(); i.hasNext(); )
        {
            final ResolutionNode node = i.next();
            List<ResolutionNode> nodes = nodesByGroupId.get( node.getArtifact().getGroupId() );
            if ( nodes == null )
            {
                nodes = new ArrayList<ResolutionNode>();
                nodesByGroupId.put( node.getArtifact().getGroupId(), nodes );
            }
            nodes.add( node );
        }

        final List<ArtifactResolutionException> resolutionExceptions =
            Collections.synchronizedList( new ArrayList<ArtifactResolutionException>() );
        try
        {
            for ( final List<ResolutionNode> nodes : nodesByGroupId.values() )
            {
                resolveArtifactPool.execute( new ResolveArtifactTask( resolveArtifactPool, latch, nodes,
                                                                      localRepository, resolvedArtifacts,
                                                                      missingArtifacts, resolutionExceptions ) );
            }

            latch.await();
        }
        catch ( final InterruptedException e )
        {
            throw new ArtifactResolutionException( "Resolution interrupted", originatingArtifact, e );
        }

        if ( !resolutionExceptions.isEmpty() )
        {
            throw resolutionExceptions.get( 0 );
        }

        if ( missingArtifacts.size() > 0 )
        {
            throw new MultipleArtifactsNotFoundException( originatingArtifact, resolvedArtifacts, missingArtifacts,
                                                          getMirroredRepositories( remoteRepositories ) );
        }

        return artifactResolutionResult;
    }

    protected List<ArtifactRepository> getMirroredRepositories( final List<ArtifactRepository> remoteRepositories )
    {
        final Map<String, ArtifactRepository> repos = new LinkedHashMap<String, ArtifactRepository>();
        for ( final ArtifactRepository repository : remoteRepositories )
        {
            final ArtifactRepository repo = wagonManager.getMirrorRepository( repository );
            repos.put( repo.getId(), repo );
        }

        return new ArrayList<ArtifactRepository>( repos.values() );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final List remoteRepositories,
                                                         final ArtifactRepository localRepository,
                                                         final ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, localRepository, remoteRepositories, source, null );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final List remoteRepositories,
                                                         final ArtifactRepository localRepository,
                                                         final ArtifactMetadataSource source, final List listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return resolveTransitively( artifacts, originatingArtifact, Collections.EMPTY_MAP, localRepository,
                                    remoteRepositories, source, null, listeners );
    }

    protected class ResolveArtifactTask
        implements Runnable
    {
        private final List<ResolutionNode> nodes;

        private final ArtifactRepository localRepository;

        private final List<Artifact> resolvedArtifacts;

        private final List<Artifact> missingArtifacts;

        private final CountDownLatch latch;

        private final ThreadPoolExecutor pool;

        private final List<ArtifactResolutionException> resolutionExceptions;

        public ResolveArtifactTask( final ThreadPoolExecutor pool, final CountDownLatch latch,
                                    final List<ResolutionNode> nodes, final ArtifactRepository localRepository,
                                    final List<Artifact> resolvedArtifacts, final List<Artifact> missingArtifacts,
                                    final List<ArtifactResolutionException> resolutionExceptions )
        {
            this.nodes = nodes;
            this.localRepository = localRepository;
            this.resolvedArtifacts = resolvedArtifacts;
            this.missingArtifacts = missingArtifacts;
            this.latch = latch;
            this.pool = pool;
            this.resolutionExceptions = resolutionExceptions;
        }

        public void run()
        {
            final Iterator<ResolutionNode> i = nodes.iterator();
            final ResolutionNode node = i.next();
            i.remove();
            try
            {
                resolveArtifact( node );
            }
            catch ( final ArtifactResolutionException e )
            {
                resolutionExceptions.add( e );
            }
            finally
            {
                latch.countDown();

                if ( i.hasNext() )
                {
                    pool.execute( new ResolveArtifactTask( pool, latch, nodes, localRepository, resolvedArtifacts,
                                                           missingArtifacts, resolutionExceptions ) );
                }
            }
        }

        protected void resolveArtifact( final ResolutionNode node )
            throws ArtifactResolutionException
        {
            try
            {
                resolve( node.getArtifact(), node.getRemoteRepositories(), localRepository );
                resolvedArtifacts.add( node.getArtifact() );
            }
            catch ( final ArtifactNotFoundException anfe )
            {
                getLogger().debug( anfe.getMessage(), anfe );

                missingArtifacts.add( node.getArtifact() );
            }
        }
    }

    public synchronized void configureNumberOfThreads( final int threads )
    {
        resolveArtifactPool.setCorePoolSize( threads );
        resolveArtifactPool.setMaximumPoolSize( threads );
    }

    protected void setWagonManager( final WagonManager wagonManager )
    {
        this.wagonManager = wagonManager;
    }
}
