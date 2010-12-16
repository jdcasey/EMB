/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonjava.emb.version.resolve;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.EMBException;
import org.commonjava.emb.boot.embed.EMBEmbedder;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.internal.EnhancedLocalRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component( role = SessionManager.class )
public class SessionManager
{

    @Requirement
    private EMBEmbedder emb;

    @Requirement
    private RepositorySystem mavenRepositorySystem;

    public synchronized ProjectBuildingRequest getProjectBuildingRequest( final DependencyResolverSession session )
        throws DependencyResolverException
    {
        ProjectBuildingRequest pbr = session.getProjectBuildingRequest();
        try
        {
            if ( pbr == null )
            {
                pbr = emb.serviceManager().createProjectBuildingRequest();

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
        catch ( final EMBException e )
        {
            throw new DependencyResolverException( "Failed to create project-building request: %s", e, e.getMessage() );
        }
        catch ( final InvalidRepositoryException e )
        {
            throw new DependencyResolverException( "Failed to create local-repository instance. Reason: %s", e,
                                                   e.getMessage() );
        }

        return pbr;
    }

    public RepositorySystemSession getRepositorySystemSession( final DependencyResolverSession session )
        throws EMBException
    {
        final File localRepo = session.getLocalRepositoryDirectory();
        localRepo.mkdirs();

        RepositorySystemSession sess = session.getRepositorySystemSession();
        if ( sess == null )
        {
            final DefaultRepositorySystemSession rss =
                new DefaultRepositorySystemSession( emb.serviceManager().createAetherRepositorySystemSession() );

            // session.setWorkspaceReader( new ImportWorkspaceReader( workspace ) );
            rss.setConfigProperty( DependencyResolverSession.SESSION_KEY, session );
            rss.setLocalRepositoryManager( new EnhancedLocalRepositoryManager( localRepo ) );
            rss.setWorkspaceReader( new SessionWorkspaceReader( session ) );

            sess = rss;

            session.setRepositorySystemSession( sess );
        }

        return sess;
    }

    public synchronized List<RemoteRepository> getRemoteRepositories( final DependencyResolverSession session )
        throws DependencyResolverException
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

    public synchronized List<ArtifactRepository> getArtifactRepositories( final DependencyResolverSession session )
        throws DependencyResolverException
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
                        throw new DependencyResolverException(
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
                throw new DependencyResolverException( "Failed to create default (central) repository instance: %s", e,
                                                       e.getMessage() );
            }

            session.setRemoteArtifactRepositories( repos );
        }

        return repos;
    }

}
