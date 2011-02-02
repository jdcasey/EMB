/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.project;

import static org.apache.maven.artifact.ArtifactUtils.key;

import org.apache.log4j.Logger;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactTypeRegistry;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.graph.Exclusion;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.JavaScopes;
import org.sonatype.aether.util.graph.selector.ScopeDependencySelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component( role = DependencyGraphResolver.class )
public class DependencyGraphResolver
{

    private static final Logger LOGGER = Logger.getLogger( DependencyGraphResolver.class );

    @Requirement
    private RepositorySystem repositorySystem;

    public DependencyGraphTracker resolveGraph( final Collection<MavenProject> rootProjects,
                                                RepositorySystemSession rss, final ProjectToolsSession session )
    {
        rss = prepareForGraphResolution( rss );

        final DependencyGraphTracker graphState =
            accumulate( session, rss, rootProjects, session.getRemoteRepositoriesArray() );

        resolve( rss, rootProjects, graphState );

        LOGGER.info( "Graph state contains: " + graphState.size() + " nodes." );

        return graphState;
    }

    // TODO: Allow fine-tuning of scopes resolved...
    private RepositorySystemSession prepareForGraphResolution( final RepositorySystemSession s )
    {
        final DefaultRepositorySystemSession session = new DefaultRepositorySystemSession( s );
        session.setDependencySelector( new ScopeDependencySelector() );

        return session;
    }

    private void resolve( final RepositorySystemSession session, final Collection<MavenProject> rootProjects,
                          final DependencyGraphTracker graphState )
    {
        final Set<String> rootProjectIds = new HashSet<String>();
        for ( final MavenProject project : rootProjects )
        {
            rootProjectIds.add( key( project.getGroupId(), project.getArtifactId(), project.getVersion() ) );
        }

        final Set<DependencyResolveWorker> workers = new HashSet<DependencyResolveWorker>();
        for ( final DependencyTracker depState : graphState.getDependencyTrackers() )
        {
            if ( depState == null || depState.hasErrors() || rootProjectIds.contains( depState.getProjectId() ) )
            {
                continue;
            }

            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "Resolving: " + depState.getLatestArtifact() );
            }
            workers.add( new DependencyResolveWorker( depState, session, repositorySystem ) );
        }

        runResolve( workers );
        // for ( final DependencyResolveWorker worker : workers )
        // {
        // worker.run();
        // }

        if ( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug( "Dependency-graph resolution complete." );
        }
    }

    private void runResolve( final Set<DependencyResolveWorker> workers )
    {
        final ExecutorService executorService = Executors.newFixedThreadPool( 1 );

        final CountDownLatch latch = new CountDownLatch( workers.size() );
        for ( final DependencyResolveWorker worker : workers )
        {
            worker.setLatch( latch );
            executorService.execute( worker );
        }

        synchronized ( latch )
        {
            long count = 0;
            while ( ( count = latch.getCount() ) > 0 )
            {
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( count + " resolution workers remaining. Waiting 3s..." );
                }
                try
                {
                    latch.await( 3, TimeUnit.SECONDS );
                }
                catch ( final InterruptedException e )
                {
                    break;
                }
            }
        }

        boolean terminated = false;
        int count = 1;
        while ( !terminated )
        {
            try
            {
                executorService.shutdown();
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( "Attempt " + count + " to shutdown graph-resolver. Waiting 3s..." );
                }

                count++;
                terminated = executorService.awaitTermination( 3, TimeUnit.SECONDS );
            }
            catch ( final InterruptedException e )
            {
                break;
            }
        }
    }

    private DependencyGraphTracker accumulate( final ProjectToolsSession session, final RepositorySystemSession rss,
                                               final Collection<MavenProject> projects,
                                               final RemoteRepository... remoteRepositories )
    {
        final ArtifactTypeRegistry stereotypes = rss.getArtifactTypeRegistry();

        final DependencyGraphTracker graphState = session.getGraphTracker();
        final GraphAccumulator accumulator = new GraphAccumulator( graphState );

        for ( final MavenProject project : projects )
        {
            final CollectRequest request = new CollectRequest();
            request.setRequestContext( "project" );
            request.setRepositories( Arrays.asList( remoteRepositories ) );

            if ( project.getDependencyArtifacts() == null )
            {
                for ( final Dependency dependency : project.getDependencies() )
                {
                    request.addDependency( RepositoryUtils.toDependency( dependency, stereotypes ) );
                }
            }
            else
            {
                final Map<String, Dependency> dependencies = new HashMap<String, Dependency>();
                for ( final Dependency dependency : project.getDependencies() )
                {
                    final String key = dependency.getManagementKey();
                    dependencies.put( key, dependency );
                }
                for ( final org.apache.maven.artifact.Artifact artifact : project.getDependencyArtifacts() )
                {
                    final String key = artifact.getDependencyConflictId();
                    final Dependency dependency = dependencies.get( key );
                    final Collection<org.apache.maven.model.Exclusion> exclusions =
                        dependency != null ? dependency.getExclusions() : null;
                    org.sonatype.aether.graph.Dependency dep = RepositoryUtils.toDependency( artifact, exclusions );
                    if ( !JavaScopes.SYSTEM.equals( dep.getScope() ) && dep.getArtifact().getFile() != null )
                    {
                        // enable re-resolution
                        org.sonatype.aether.artifact.Artifact art = dep.getArtifact();
                        art = art.setFile( null ).setVersion( art.getBaseVersion() );
                        dep = dep.setArtifact( art );
                    }
                    request.addDependency( dep );
                }
            }

            final DependencyManagement depMngt = project.getDependencyManagement();
            if ( depMngt != null )
            {
                for ( final Dependency dependency : depMngt.getDependencies() )
                {
                    request.addManagedDependency( RepositoryUtils.toDependency( dependency, stereotypes ) );
                }
            }

            CollectResult result;
            try
            {
                result = repositorySystem.collectDependencies( rss, request );
            }
            catch ( final DependencyCollectionException e )
            {
                // TODO: Handle problem resolving POMs...
                result = e.getResult();

                // result.setDependencyGraph( e.getResult().getRoot() );
                // result.setCollectionErrors( e.getResult().getExceptions() );
                //
                // throw new DependencyResolutionException( result, "Could not resolve dependencies for project "
                // + project.getId() + ": " + e.getMessage(), e );
            }

            graphState.addGraphRoot( project, result.getRoot() );
            result.getRoot().accept( accumulator );

            accumulator.resetForNextRun();
        }

        return graphState;
    }

    private static final class GraphAccumulator
        implements DependencyVisitor
    {
        private final LinkedList<DependencyNode> parents = new LinkedList<DependencyNode>();

        private final Set<Exclusion> exclusions = new HashSet<Exclusion>();

        private final Set<Exclusion> lastExclusions = new HashSet<Exclusion>();

        private final DependencyGraphTracker graphState;

        GraphAccumulator( final DependencyGraphTracker graphState )
        {
            this.graphState = graphState;
        }

        void resetForNextRun()
        {
            parents.clear();
            exclusions.clear();
            lastExclusions.clear();
        }

        @Override
        public boolean visitEnter( final DependencyNode node )
        {
            if ( node == null || node.getDependency() == null || node.getDependency().getArtifact() == null )
            {
                return true;
            }

            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "START: dependency-processing for: " + node );
            }

            boolean result = false;
            final Artifact artifact = node.getDependency().getArtifact();
            if ( !excluded( artifact ) )
            {
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( "Enabling resolution for: " + node );
                }
                final List<DependencyNode> depTrail = new ArrayList<DependencyNode>( parents );

                graphState.track( node, depTrail );
                if ( node.getDependency().getExclusions() != null )
                {
                    for ( final Exclusion exclusion : node.getDependency().getExclusions() )
                    {
                        if ( exclusions.add( exclusion ) )
                        {
                            lastExclusions.add( exclusion );
                        }
                    }
                }

                parents.addFirst( node );

                final StringBuilder builder = new StringBuilder();
                for ( int i = 0; i < parents.size(); i++ )
                {
                    builder.append( "  " );
                }
                builder.append( ">>>" );
                builder.append( node );
                LOGGER.info( builder.toString() );

                result = true;
            }
            else
            {
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( "DISABLING resolution for: " + node );
                }
            }

            if ( node != null && !node.getRelocations().isEmpty() )
            {
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( "The artifact " + node.getRelocations().get( 0 ) + " has been relocated to "
                                    + node.getDependency().getArtifact() );
                }
            }

            return result;
        }

        private boolean excluded( final Artifact artifact )
        {
            for ( final Exclusion exclusion : exclusions )
            {
                if ( match( exclusion.getGroupId(), artifact.getGroupId() )
                                && match( exclusion.getArtifactId(), artifact.getArtifactId() )
                                && match( exclusion.getExtension(), artifact.getExtension() )
                                && match( exclusion.getClassifier(), artifact.getClassifier() ) )
                {
                    if ( LOGGER.isDebugEnabled() )
                    {
                        LOGGER.debug( "EXCLUDED: " + artifact );
                    }
                    return true;
                }
            }

            return false;
        }

        private boolean match( final String excluded, final String check )
        {
            return "*".equals( excluded ) || excluded.equals( check );
        }

        @Override
        public boolean visitLeave( final DependencyNode node )
        {
            for ( final Exclusion exclusion : lastExclusions )
            {
                exclusions.remove( exclusion );
            }

            lastExclusions.clear();

            if ( !parents.isEmpty() && node == parents.getFirst() )
            {
                final StringBuilder builder = new StringBuilder();
                for ( int i = 0; i < parents.size(); i++ )
                {
                    builder.append( "  " );
                }
                builder.append( "<<<" );
                builder.append( node );
                LOGGER.info( builder.toString() );

                parents.removeFirst();
            }
            else
            {
                LOGGER.info( "\n\nTRAVERSAL LEAK!!! " + node + "\n\n" );
            }

            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "END: dependency-processing for: " + node );
            }

            return true;
        }

    }

    private static final class DependencyResolveWorker
        implements Runnable
    {

        private final DependencyTracker depState;

        private final RepositorySystemSession session;

        private final RepositorySystem repositorySystem;

        private ArtifactResult result;

        private CountDownLatch latch;

        DependencyResolveWorker( final DependencyTracker depState, final RepositorySystemSession session,
                                 final RepositorySystem repositorySystem )
        {
            this.depState = depState;
            this.session = session;
            this.repositorySystem = repositorySystem;
        }

        void setLatch( final CountDownLatch latch )
        {
            this.latch = latch;
        }

        @Override
        public void run()
        {
            final Artifact artifact = depState.getLatestArtifact();
            try
            {
                final ArtifactRequest request =
                    new ArtifactRequest( artifact, new ArrayList<RemoteRepository>( depState.getRemoteRepositories() ),
                                         "project" );

                result = new ArtifactResult( request );
                if ( validateForResolution() )
                {
                    try
                    {
                        if ( LOGGER.isDebugEnabled() )
                        {
                            LOGGER.debug( "RESOLVE: " + artifact );
                        }

                        result = repositorySystem.resolveArtifact( session, request );
                    }
                    catch ( final ArtifactResolutionException e )
                    {
                        result.addException( e );
                    }
                }
            }
            finally
            {
                depState.setResult( result );
                if ( latch != null )
                {
                    latch.countDown();
                }
            }
        }

        private boolean validateForResolution()
        {
            boolean valid = true;
            if ( session == null )
            {
                result.addException( new IllegalArgumentException( "Cannot resolve dependency: "
                                + depState.getLatestArtifact() + ", RepositorySystemSession has not been set!" ) );

                valid = false;
            }

            if ( repositorySystem == null )
            {
                result.addException( new IllegalArgumentException( "Cannot resolve dependency: "
                                + depState.getLatestArtifact() + ", RepositorySystem has not been set!" ) );

                valid = false;
            }

            return valid;
        }
    }

}
