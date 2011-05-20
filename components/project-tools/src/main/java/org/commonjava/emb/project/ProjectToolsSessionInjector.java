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

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.mae.MAEException;
import org.apache.maven.mae.boot.embed.MAEEmbedder;
import org.apache.maven.model.Repository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.internal.EnhancedLocalRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component( role = ProjectToolsSessionInjector.class )
public class ProjectToolsSessionInjector
{

    @Requirement
    private MAEEmbedder embedder;

    @Requirement
    private RepositorySystem mavenRepositorySystem;

    public synchronized ProjectBuildingRequest getProjectBuildingRequest( final ProjectToolsSession session )
        throws ProjectToolsException
    {
        ProjectBuildingRequest pbr = session.getProjectBuildingRequest();
        try
        {
            if ( pbr == null )
            {
                pbr = embedder.serviceManager().createProjectBuildingRequest();

                pbr.setProcessPlugins( false );
                pbr.setResolveDependencies( false );
                pbr.setSystemProperties( System.getProperties() );
                pbr.setInactiveProfileIds( new ArrayList<String>() );
                pbr.setRepositoryMerging( ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT );

                final RepositorySystemSession rss = getRepositorySystemSession( session );
                pbr.setRepositorySession( rss );
                pbr.setLocalRepository( mavenRepositorySystem.createLocalRepository( rss.getLocalRepository()
                                                                                        .getBasedir() ) );
                pbr.setRemoteRepositories( getArtifactRepositories( session ) );

                session.setProjectBuildingRequest( pbr );
            }
            else
            {
                pbr = new DefaultProjectBuildingRequest( pbr );
                pbr.setRepositorySession( getRepositorySystemSession( session ) );
            }
        }
        catch ( final MAEException e )
        {
            throw new ProjectToolsException( "Failed to create project-building request: %s", e, e.getMessage() );
        }
        catch ( final InvalidRepositoryException e )
        {
            throw new ProjectToolsException( "Failed to create local-repository instance. Reason: %s", e,
                                                   e.getMessage() );
        }

        return pbr;
    }

    public RepositorySystemSession getRepositorySystemSession( final ProjectToolsSession session )
        throws MAEException
    {
        final File localRepo = session.getLocalRepositoryDirectory();
        localRepo.mkdirs();

        RepositorySystemSession sess = session.getRepositorySystemSession();
        if ( sess == null )
        {
            final DefaultRepositorySystemSession rss =
                new DefaultRepositorySystemSession( embedder.serviceManager().createAetherRepositorySystemSession( session.getExecutionRequest() ) );

            // session.setWorkspaceReader( new ImportWorkspaceReader( workspace ) );
            rss.setConfigProperty( ProjectToolsSession.SESSION_KEY, session );
            rss.setLocalRepositoryManager( new EnhancedLocalRepositoryManager( localRepo ) );
            rss.setWorkspaceReader( new SessionWorkspaceReader( session ) );

            sess = rss;

            session.setRepositorySystemSession( sess );
        }

        return sess;
    }

    public synchronized List<RemoteRepository> getRemoteRepositories( final ProjectToolsSession session )
        throws ProjectToolsException
    {
        List<RemoteRepository> result = session.getRemoteRepositories();

        if ( result == null )
        {
            result = new ArrayList<RemoteRepository>();

            for ( final ArtifactRepository repo : getArtifactRepositories( session ) )
            {
                if ( repo instanceof RemoteRepository )
                {
                    result.add( (RemoteRepository) repo );
                }
                else if ( repo instanceof MavenArtifactRepository )
                {
                    result.add( new RemoteRepository( repo.getId(), "default", repo.getUrl() ) );
                }
            }

            session.setRemoteRepositories( result );
        }

        return result;
    }

    public synchronized List<ArtifactRepository> getArtifactRepositories( final ProjectToolsSession session )
        throws ProjectToolsException
    {
        List<ArtifactRepository> repos = session.getRemoteArtifactRepositories();
        if ( repos == null )
        {
            final Repository[] remoteRepositories = session.getResolveRepositories();

            repos = new ArrayList<ArtifactRepository>( remoteRepositories == null ? 0 : remoteRepositories.length );

            if ( remoteRepositories != null )
            {
                for ( final Repository repo : remoteRepositories )
                {
                    try
                    {
                        repos.add( mavenRepositorySystem.buildArtifactRepository( repo ) );
                    }
                    catch ( final InvalidRepositoryException e )
                    {
                        throw new ProjectToolsException(
                                                               "Failed to create remote artifact repository instance from: %s\nReason: %s",
                                                               e, repo, e.getMessage() );
                    }
                }
            }

            try
            {
                repos.add( mavenRepositorySystem.createDefaultRemoteRepository() );
            }
            catch ( final InvalidRepositoryException e )
            {
                throw new ProjectToolsException( "Failed to create default (central) repository instance: %s", e,
                                                       e.getMessage() );
            }

            session.setRemoteArtifactRepositories( repos );
        }

        return repos;
    }

}
