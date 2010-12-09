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

package org.commonjava.emb.event.resolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.apache.maven.repository.ArtifactTransferFailedException;
import org.apache.maven.repository.ArtifactTransferListener;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.event.EMBEventManager;

import java.io.File;
import java.util.List;

@Component( role = RepositorySystem.class, hint = "emb-events" )
public class EventingRepositorySystem
    implements RepositorySystem
{

    @Requirement
    private EMBEventManager eventManager;

    @Requirement( hint = "#" )
    private RepositorySystem delegate;

    public ArtifactRepository buildArtifactRepository( final Repository repository )
        throws InvalidRepositoryException
    {
        return delegate.buildArtifactRepository( repository );
    }

    public Artifact createArtifact( final String groupId, final String artifactId, final String version,
                                    final String scope, final String type )
    {
        return delegate.createArtifact( groupId, artifactId, version, scope, type );
    }

    public Artifact createArtifact( final String groupId, final String artifactId, final String version,
                                    final String packaging )
    {
        return delegate.createArtifact( groupId, artifactId, version, packaging );
    }

    public ArtifactRepository createArtifactRepository( final String id, final String url,
                                                        final ArtifactRepositoryLayout repositoryLayout,
                                                        final ArtifactRepositoryPolicy snapshots,
                                                        final ArtifactRepositoryPolicy releases )
    {
        return delegate.createArtifactRepository( id, url, repositoryLayout, snapshots, releases );
    }

    public Artifact createArtifactWithClassifier( final String groupId, final String artifactId, final String version,
                                                  final String type, final String classifier )
    {
        return delegate.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
    }

    public ArtifactRepository createDefaultLocalRepository()
        throws InvalidRepositoryException
    {
        return delegate.createDefaultLocalRepository();
    }

    public ArtifactRepository createDefaultRemoteRepository()
        throws InvalidRepositoryException
    {
        return delegate.createDefaultRemoteRepository();
    }

    public Artifact createDependencyArtifact( final Dependency dependency )
    {
        return delegate.createDependencyArtifact( dependency );
    }

    public ArtifactRepository createLocalRepository( final File localRepository )
        throws InvalidRepositoryException
    {
        return delegate.createLocalRepository( localRepository );
    }

    public Artifact createPluginArtifact( final Plugin plugin )
    {
        return delegate.createPluginArtifact( plugin );
    }

    public Artifact createProjectArtifact( final String groupId, final String artifactId, final String version )
    {
        return delegate.createProjectArtifact( groupId, artifactId, version );
    }

    public List<ArtifactRepository> getEffectiveRepositories( final List<ArtifactRepository> repositories )
    {
        return delegate.getEffectiveRepositories( repositories );
    }

    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        return delegate.getMirror( repository, mirrors );
    }

    public void injectAuthentication( final List<ArtifactRepository> repositories, final List<Server> servers )
    {
        delegate.injectAuthentication( repositories, servers );
    }

    public void injectMirror( final List<ArtifactRepository> repositories, final List<Mirror> mirrors )
    {
        delegate.injectMirror( repositories, mirrors );
    }

    public void injectProxy( final List<ArtifactRepository> repositories, final List<Proxy> proxies )
    {
        delegate.injectProxy( repositories, proxies );
    }

    public void publish( final ArtifactRepository repository, final File source, final String remotePath,
                         final ArtifactTransferListener transferListener )
        throws ArtifactTransferFailedException
    {
        delegate.publish( repository, source, remotePath, transferListener );
    }

    public ArtifactResolutionResult resolve( final ArtifactResolutionRequest request )
    {
        eventManager.fireEvent( new ArtifactResolutionEvent( request ) );

        final ArtifactResolutionResult result = delegate.resolve( request );

        eventManager.fireEvent( new ArtifactResolutionEvent( request, result ) );

        return result;
    }

    public void retrieve( final ArtifactRepository repository, final File destination, final String remotePath,
                          final ArtifactTransferListener transferListener )
        throws ArtifactTransferFailedException, ArtifactDoesNotExistException
    {
        delegate.retrieve( repository, destination, remotePath, transferListener );
    }

}
