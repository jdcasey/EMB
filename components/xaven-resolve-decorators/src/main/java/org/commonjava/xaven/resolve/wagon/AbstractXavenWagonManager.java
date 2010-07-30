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

package org.commonjava.xaven.resolve.wagon;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.commonjava.xaven.resolve.event.EventDispatcher;
import org.commonjava.xaven.resolve.event.EventStatus;
import org.commonjava.xaven.resolve.util.ContextBuilder;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractXavenWagonManager
    implements WagonManager
{

    @Inject
    @Named( "wagonManager" )
    private EventDispatcher<WagonManagerEvent> eventDispatcher;

    protected abstract WagonManager getDelegate();

    public void addAuthenticationInfo( final String repositoryId, final String username, final String password,
                                       final String privateKey, final String passphrase )
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "repositoryId", repositoryId )
                                .with( "username", username )
                                .with( "password", password )
                                .with( "privateKey", privateKey )
                                .with( "passphrase", passphrase )
                                .map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.addAuthenticationInfo, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().addAuthenticationInfo( repositoryId, username, password, privateKey, passphrase );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void addConfiguration( final String repositoryId, final Xpp3Dom configuration )
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "repositoryId", repositoryId ).with( "configuration", configuration ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.addConfiguration, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().addConfiguration( repositoryId, configuration );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void addMirror( final String id, final String mirrorOf, final String url )
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "id", id ).with( "mirrorOf", mirrorOf ).with( "url", url ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.addMirror, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().addMirror( id, mirrorOf, url );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void addPermissionInfo( final String repositoryId, final String filePermissions,
                                   final String directoryPermissions )
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "repositoryId", repositoryId )
                                .with( "filePermissions", filePermissions )
                                .with( "directoryPermissions", directoryPermissions )
                                .map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.addPermissionInfo, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().addPermissionInfo( repositoryId, filePermissions, directoryPermissions );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void addProxy( final String protocol, final String host, final int port, final String username,
                          final String password, final String nonProxyHosts )
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "protocol", protocol )
                                .with( "host", host )
                                .with( "port", port )
                                .with( "username", username )
                                .with( "password", password )
                                .with( "nonProxyHosts", nonProxyHosts )
                                .map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.addProxy, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().addProxy( protocol, host, port, username, password, nonProxyHosts );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void getArtifact( final Artifact artifact, final ArtifactRepository repository )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "artifact", artifact ).with( "repository", repository ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getArtifact, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        try
        {
            getDelegate().getArtifact( artifact, repository );

            eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
        }
        catch ( final TransferFailedException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
        catch ( final ResourceDoesNotExistException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
    }

    @SuppressWarnings( "unchecked" )
    public void getArtifact( final Artifact artifact, final List remoteRepositories )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "artifact", artifact ).with( "remoteRepositories", remoteRepositories ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getArtifact, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        try
        {
            getDelegate().getArtifact( artifact, remoteRepositories );

            eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
        }
        catch ( final TransferFailedException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
        catch ( final ResourceDoesNotExistException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
    }

    public void getArtifactMetadata( final ArtifactMetadata metadata, final ArtifactRepository remoteRepository,
                                     final File destination, final String checksumPolicy )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "metadata", metadata )
                                .with( "remoteRepository", remoteRepository )
                                .with( "destination", destination )
                                .with( "checksumPolicy", checksumPolicy )
                                .map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getArtifactMetadata, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        try
        {
            getDelegate().getArtifactMetadata( metadata, remoteRepository, destination, checksumPolicy );

            eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
        }
        catch ( final TransferFailedException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
        catch ( final ResourceDoesNotExistException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
    }

    public void getArtifactMetadataFromDeploymentRepository( final ArtifactMetadata metadata,
                                                             final ArtifactRepository remoteRepository,
                                                             final File file, final String checksumPolicyWarn )
        throws TransferFailedException, ResourceDoesNotExistException
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "metadata", metadata )
                                .with( "remoteRepository", remoteRepository )
                                .with( "destination", file )
                                .with( "checksumPolicy", checksumPolicyWarn )
                                .map();

        final WagonManagerEvent evt =
            new WagonManagerEvent( WagonEventType.getArtifactMetadataFromDeploymentRepository, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        try
        {
            getDelegate().getArtifactMetadataFromDeploymentRepository( metadata, remoteRepository, file,
                                                                       checksumPolicyWarn );

            eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
        }
        catch ( final TransferFailedException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
        catch ( final ResourceDoesNotExistException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
    }

    public AuthenticationInfo getAuthenticationInfo( final String id )
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "id", id ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getAuthenticationInfo, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        final AuthenticationInfo result = getDelegate().getAuthenticationInfo( id );

        eventDispatcher.dispatch( evt.withAdditionalParameter( "result", result ), EventStatus.AFTER_SUCCESS );

        return result;
    }

    public ArtifactRepository getMirrorRepository( final ArtifactRepository repository )
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "repository", repository ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getMirrorRepository, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        final ArtifactRepository result = getDelegate().getMirrorRepository( repository );

        eventDispatcher.dispatch( evt.withAdditionalParameter( "result", result ), EventStatus.AFTER_SUCCESS );

        return result;
    }

    public ProxyInfo getProxy( final String protocol )
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "protocol", protocol ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getProxy, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        final ProxyInfo result = getDelegate().getProxy( protocol );

        eventDispatcher.dispatch( evt.withAdditionalParameter( "result", result ), EventStatus.AFTER_SUCCESS );

        return result;
    }

    public Wagon getWagon( final Repository repository )
        throws UnsupportedProtocolException, WagonConfigurationException
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "repository", repository ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getWagon, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        final Wagon result = getDelegate().getWagon( repository );

        eventDispatcher.dispatch( evt.withAdditionalParameter( "result", result ), EventStatus.AFTER_SUCCESS );

        return result;
    }

    @Deprecated
    public Wagon getWagon( final String protocol )
        throws UnsupportedProtocolException
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "protocol", protocol ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.getWagon, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        final Wagon result = getDelegate().getWagon( protocol );

        eventDispatcher.dispatch( evt.withAdditionalParameter( "result", result ), EventStatus.AFTER_SUCCESS );

        return result;
    }

    public boolean isOnline()
    {
        final Map<String, Object> ctx = new ContextBuilder().map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.isOnline, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        final boolean result = getDelegate().isOnline();

        eventDispatcher.dispatch( evt.withAdditionalParameter( "result", result ), EventStatus.AFTER_SUCCESS );

        return result;
    }

    public void putArtifact( final File source, final Artifact artifact, final ArtifactRepository deploymentRepository )
        throws TransferFailedException
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "source", source )
                                .with( "artifact", artifact )
                                .with( "deploymentRepository", deploymentRepository )
                                .map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.putArtifact, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        try
        {
            getDelegate().putArtifact( source, artifact, deploymentRepository );

            eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
        }
        catch ( final TransferFailedException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
    }

    public void putArtifactMetadata( final File source, final ArtifactMetadata artifactMetadata,
                                     final ArtifactRepository repository )
        throws TransferFailedException
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "source", source )
                                .with( "artifactMetadata", artifactMetadata )
                                .with( "deploymentRepository", repository )
                                .map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.putArtifactMetadata, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        try
        {
            getDelegate().putArtifactMetadata( source, artifactMetadata, repository );

            eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
        }
        catch ( final TransferFailedException e )
        {
            eventDispatcher.dispatch( evt.withAdditionalParameter( "error", e ), EventStatus.AFTER_ERROR );
            throw e;
        }
    }

    @SuppressWarnings( "unchecked" )
    public void registerWagons( final Collection wagons, final PlexusContainer extensionContainer )
    {
        final Map<String, Object> ctx =
            new ContextBuilder().with( "wagons", wagons ).with( "extensionContainer", extensionContainer ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.registerWagons, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().registerWagons( wagons, extensionContainer );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void setDefaultRepositoryPermissions( final RepositoryPermissions permissions )
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "permissions", permissions ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.setDefaultRepositoryPermissions, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().setDefaultRepositoryPermissions( permissions );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void setDownloadMonitor( final TransferListener downloadMonitor )
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "downloadMonitor", downloadMonitor ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.setDownloadMonitor, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().setDownloadMonitor( downloadMonitor );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );
    }

    public void setInteractive( final boolean interactive )
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "interactive", interactive ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.setInteractive, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().setInteractive( interactive );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

    public void setOnline( final boolean online )
    {
        final Map<String, Object> ctx = new ContextBuilder().with( "online", online ).map();

        final WagonManagerEvent evt = new WagonManagerEvent( WagonEventType.setOnline, ctx );

        eventDispatcher.dispatch( evt, EventStatus.BEFORE_ACTION );

        getDelegate().setOnline( online );

        eventDispatcher.dispatch( evt, EventStatus.AFTER_SUCCESS );
    }

}
