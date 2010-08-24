package org.commonjava.xaven.component.vscheme.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.repository.ArtifactDoesNotExistException;
import org.apache.maven.repository.ArtifactTransferFailedException;
import org.apache.maven.repository.ArtifactTransferListener;
import org.apache.maven.repository.DelegatingLocalArtifactRepository;
import org.apache.maven.repository.LocalArtifactRepository;
import org.apache.maven.repository.MetadataResolutionRequest;
import org.apache.maven.repository.MetadataResolutionResult;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.repository.Proxy;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.legacy.TransferListenerAdapter;
import org.apache.maven.repository.legacy.WagonManager;
import org.apache.maven.repository.legacy.repository.ArtifactRepositoryFactory;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jason van Zyl
 */
@Component( role = RepositorySystem.class, hint = "vscheme" )
public class VersionSchemeRepositorySystem
    implements RepositorySystem
{

    @Requirement
    private Logger logger;

    @Requirement
    private ArtifactFactory artifactFactory;

    @Requirement
    private ArtifactResolver artifactResolver;

    @Requirement
    private ArtifactRepositoryFactory artifactRepositoryFactory;

    @Requirement( role = ArtifactRepositoryLayout.class )
    private Map<String, ArtifactRepositoryLayout> layouts;

    @Requirement
    private WagonManager wagonManager;

    @Requirement
    private PlexusContainer plexus;

    @Requirement
    private MirrorSelector mirrorSelector;

    @Requirement
    private SettingsDecrypter settingsDecrypter;

    public Artifact createArtifact( final String groupId, final String artifactId, final String version,
                                    final String scope, final String type )
    {
        return artifactFactory.createArtifact( groupId, artifactId, version, scope, type );
    }

    public Artifact createArtifact( final String groupId, final String artifactId, final String version,
                                    final String packaging )
    {
        return artifactFactory.createBuildArtifact( groupId, artifactId, version, packaging );
    }

    public Artifact createArtifactWithClassifier( final String groupId, final String artifactId, final String version,
                                                  final String type, final String classifier )
    {
        return artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
    }

    public Artifact createProjectArtifact( final String groupId, final String artifactId, final String metaVersionId )
    {
        return artifactFactory.createProjectArtifact( groupId, artifactId, metaVersionId );
    }

    public Artifact createDependencyArtifact( final Dependency d )
    {
        VersionRange versionRange;
        try
        {
            versionRange = VersionRange.createFromVersionSpec( d.getVersion() );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            return null;
        }

        final Artifact artifact =
            artifactFactory.createDependencyArtifact( d.getGroupId(), d.getArtifactId(), versionRange, d.getType(),
                                                      d.getClassifier(), d.getScope(), d.isOptional() );

        if ( Artifact.SCOPE_SYSTEM.equals( d.getScope() ) && d.getSystemPath() != null )
        {
            artifact.setFile( new File( d.getSystemPath() ) );
        }

        if ( !d.getExclusions().isEmpty() )
        {
            final List<String> exclusions = new ArrayList<String>();

            for ( final Exclusion exclusion : d.getExclusions() )
            {
                exclusions.add( exclusion.getGroupId() + ':' + exclusion.getArtifactId() );
            }

            artifact.setDependencyFilter( new ExcludesArtifactFilter( exclusions ) );
        }

        return artifact;
    }

    public Artifact createExtensionArtifact( final String groupId, final String artifactId, final String version )
    {
        VersionRange versionRange;
        try
        {
            versionRange = VersionRange.createFromVersionSpec( version );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            return null;
        }

        return artifactFactory.createExtensionArtifact( groupId, artifactId, versionRange );
    }

    public Artifact createParentArtifact( final String groupId, final String artifactId, final String version )
    {
        return artifactFactory.createParentArtifact( groupId, artifactId, version );
    }

    public Artifact createPluginArtifact( final Plugin plugin )
    {
        VersionRange versionRange;
        try
        {
            versionRange = VersionRange.createFromVersionSpec( plugin.getVersion() );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            return null;
        }

        return artifactFactory.createPluginArtifact( plugin.getGroupId(), plugin.getArtifactId(), versionRange );
    }

    public ArtifactRepositoryPolicy buildArtifactRepositoryPolicy( final RepositoryPolicy policy )
    {
        boolean enabled = true;

        String updatePolicy = null;

        String checksumPolicy = null;

        if ( policy != null )
        {
            enabled = policy.isEnabled();

            if ( policy.getUpdatePolicy() != null )
            {
                updatePolicy = policy.getUpdatePolicy();
            }
            if ( policy.getChecksumPolicy() != null )
            {
                checksumPolicy = policy.getChecksumPolicy();
            }
        }

        return new ArtifactRepositoryPolicy( enabled, updatePolicy, checksumPolicy );
    }

    public ArtifactRepository createDefaultLocalRepository()
        throws InvalidRepositoryException
    {
        return createLocalRepository( RepositorySystem.defaultUserLocalRepository );
    }

    public ArtifactRepository createLocalRepository( final File localRepository )
        throws InvalidRepositoryException
    {
        return createRepository( "file://" + localRepository.toURI().getRawPath(),
                                 RepositorySystem.DEFAULT_LOCAL_REPO_ID, true,
                                 ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, true,
                                 ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                 ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE );
    }

    public ArtifactRepository createDefaultRemoteRepository()
        throws InvalidRepositoryException
    {
        return createRepository( RepositorySystem.DEFAULT_REMOTE_REPO_URL, RepositorySystem.DEFAULT_REMOTE_REPO_ID,
                                 true, ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY, false,
                                 ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY,
                                 ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN );
    }

    public ArtifactRepository createLocalRepository( final String url, final String repositoryId )
        throws IOException
    {
        return createRepository( canonicalFileUrl( url ), repositoryId, true,
                                 ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, true,
                                 ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
                                 ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE );
    }

    private String canonicalFileUrl( String url )
        throws IOException
    {
        if ( !url.startsWith( "file:" ) )
        {
            url = "file://" + url;
        }
        else if ( url.startsWith( "file:" ) && !url.startsWith( "file://" ) )
        {
            url = "file://" + url.substring( "file:".length() );
        }

        // So now we have an url of the form file://<path>

        // We want to eliminate any relative path nonsense and lock down the path so we
        // need to fully resolve it before any sub-modules use the path. This can happen
        // when you are using a custom settings.xml that contains a relative path entry
        // for the local repository setting.

        final File localRepository = new File( url.substring( "file://".length() ) );

        if ( !localRepository.isAbsolute() )
        {
            url = "file://" + localRepository.getCanonicalPath();
        }

        return url;
    }

    public ArtifactResolutionResult resolve( final ArtifactResolutionRequest request )
    {
        /*
         * Probably is not worth it, but here I make sure I restore request
         * to its original state.
         */
        try
        {
            final LocalArtifactRepository ideWorkspace =
                plexus.lookup( LocalArtifactRepository.class, LocalArtifactRepository.IDE_WORKSPACE );

            if ( request.getLocalRepository() instanceof DelegatingLocalArtifactRepository )
            {
                final DelegatingLocalArtifactRepository delegatingLocalRepository =
                    (DelegatingLocalArtifactRepository) request.getLocalRepository();

                final LocalArtifactRepository orig = delegatingLocalRepository.getIdeWorspace();

                delegatingLocalRepository.setIdeWorkspace( ideWorkspace );

                try
                {
                    return artifactResolver.resolve( request );
                }
                finally
                {
                    delegatingLocalRepository.setIdeWorkspace( orig );
                }
            }
            else
            {
                final ArtifactRepository localRepository = request.getLocalRepository();
                final DelegatingLocalArtifactRepository delegatingLocalRepository =
                    new DelegatingLocalArtifactRepository( localRepository );
                delegatingLocalRepository.setIdeWorkspace( ideWorkspace );
                request.setLocalRepository( delegatingLocalRepository );
                try
                {
                    return artifactResolver.resolve( request );
                }
                finally
                {
                    request.setLocalRepository( localRepository );
                }
            }
        }
        catch ( final ComponentLookupException e )
        {
            // no ide workspace artifact resolution
        }

        return artifactResolver.resolve( request );
    }

    /*
    public void addProxy( String protocol, String host, int port, String username, String password, String nonProxyHosts )
    {
        ProxyInfo proxyInfo = new ProxyInfo();
        proxyInfo.setHost( host );
        proxyInfo.setType( protocol );
        proxyInfo.setPort( port );
        proxyInfo.setNonProxyHosts( nonProxyHosts );
        proxyInfo.setUserName( username );
        proxyInfo.setPassword( password );

        proxies.put( protocol, proxyInfo );

        wagonManager.addProxy( protocol, host, port, username, password, nonProxyHosts );
    }
    */

    public List<ArtifactRepository> getEffectiveRepositories( final List<ArtifactRepository> repositories )
    {
        if ( repositories == null )
        {
            return null;
        }

        final Map<String, List<ArtifactRepository>> reposByKey = new LinkedHashMap<String, List<ArtifactRepository>>();

        for ( final ArtifactRepository repository : repositories )
        {
            final String key = repository.getId();

            List<ArtifactRepository> aliasedRepos = reposByKey.get( key );

            if ( aliasedRepos == null )
            {
                aliasedRepos = new ArrayList<ArtifactRepository>();
                reposByKey.put( key, aliasedRepos );
            }

            aliasedRepos.add( repository );
        }

        final List<ArtifactRepository> effectiveRepositories = new ArrayList<ArtifactRepository>();

        for ( final List<ArtifactRepository> aliasedRepos : reposByKey.values() )
        {
            final List<ArtifactRepositoryPolicy> releasePolicies =
                new ArrayList<ArtifactRepositoryPolicy>( aliasedRepos.size() );

            for ( final ArtifactRepository aliasedRepo : aliasedRepos )
            {
                releasePolicies.add( aliasedRepo.getReleases() );
            }

            final ArtifactRepositoryPolicy releasePolicy = getEffectivePolicy( releasePolicies );

            final List<ArtifactRepositoryPolicy> snapshotPolicies =
                new ArrayList<ArtifactRepositoryPolicy>( aliasedRepos.size() );

            for ( final ArtifactRepository aliasedRepo : aliasedRepos )
            {
                snapshotPolicies.add( aliasedRepo.getSnapshots() );
            }

            final ArtifactRepositoryPolicy snapshotPolicy = getEffectivePolicy( snapshotPolicies );

            final ArtifactRepository aliasedRepo = aliasedRepos.get( 0 );

            final ArtifactRepository effectiveRepository =
                createArtifactRepository( aliasedRepo.getId(), aliasedRepo.getUrl(), aliasedRepo.getLayout(),
                                          snapshotPolicy, releasePolicy );

            effectiveRepository.setAuthentication( aliasedRepo.getAuthentication() );

            effectiveRepository.setProxy( aliasedRepo.getProxy() );

            effectiveRepositories.add( effectiveRepository );
        }

        return effectiveRepositories;
    }

    private ArtifactRepositoryPolicy getEffectivePolicy( final Collection<ArtifactRepositoryPolicy> policies )
    {
        ArtifactRepositoryPolicy effectivePolicy = null;

        for ( final ArtifactRepositoryPolicy policy : policies )
        {
            if ( effectivePolicy == null )
            {
                effectivePolicy = new ArtifactRepositoryPolicy( policy );
            }
            else
            {
                effectivePolicy.merge( policy );
            }
        }

        return effectivePolicy;
    }

    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        return mirrorSelector.getMirror( repository, mirrors );
    }

    public void injectMirror( final List<ArtifactRepository> repositories, final List<Mirror> mirrors )
    {
        if ( repositories != null && mirrors != null )
        {
            for ( final ArtifactRepository repository : repositories )
            {
                final Mirror mirror = getMirror( repository, mirrors );

                if ( mirror != null )
                {
                    repository.setId( mirror.getId() );
                    repository.setUrl( mirror.getUrl() );

                    final ArtifactRepositoryLayout layout = layouts.get( mirror.getLayout() );
                    if ( layout != null )
                    {
                        repository.setLayout( layout );
                    }
                }
            }
        }
    }

    public void injectAuthentication( final List<ArtifactRepository> repositories, final List<Server> servers )
    {
        if ( repositories != null )
        {
            final Map<String, Server> serversById = new HashMap<String, Server>();

            if ( servers != null )
            {
                for ( final Server server : servers )
                {
                    if ( !serversById.containsKey( server.getId() ) )
                    {
                        serversById.put( server.getId(), server );
                    }
                }
            }

            for ( final ArtifactRepository repository : repositories )
            {
                Server server = serversById.get( repository.getId() );

                if ( server != null )
                {
                    final SettingsDecryptionRequest request = new DefaultSettingsDecryptionRequest( server );
                    final SettingsDecryptionResult result = settingsDecrypter.decrypt( request );
                    server = result.getServer();

                    if ( logger.isDebugEnabled() )
                    {
                        for ( final SettingsProblem problem : result.getProblems() )
                        {
                            logger.debug( problem.getMessage(), problem.getException() );
                        }
                    }

                    final Authentication authentication =
                        new Authentication( server.getUsername(), server.getPassword() );

                    repository.setAuthentication( authentication );
                }
                else
                {
                    repository.setAuthentication( null );
                }
            }
        }
    }

    private org.apache.maven.settings.Proxy getProxy( final ArtifactRepository repository,
                                                      final List<org.apache.maven.settings.Proxy> proxies )
    {
        if ( proxies != null && repository.getProtocol() != null )
        {
            for ( final org.apache.maven.settings.Proxy proxy : proxies )
            {
                if ( proxy.isActive() && repository.getProtocol().equalsIgnoreCase( proxy.getProtocol() ) )
                {
                    return proxy;
                }
            }
        }

        return null;
    }

    public void injectProxy( final List<ArtifactRepository> repositories,
                             final List<org.apache.maven.settings.Proxy> proxies )
    {
        if ( repositories != null )
        {
            for ( final ArtifactRepository repository : repositories )
            {
                org.apache.maven.settings.Proxy proxy = getProxy( repository, proxies );

                if ( proxy != null )
                {
                    final SettingsDecryptionRequest request = new DefaultSettingsDecryptionRequest( proxy );
                    final SettingsDecryptionResult result = settingsDecrypter.decrypt( request );
                    proxy = result.getProxy();

                    if ( logger.isDebugEnabled() )
                    {
                        for ( final SettingsProblem problem : result.getProblems() )
                        {
                            logger.debug( problem.getMessage(), problem.getException() );
                        }
                    }

                    final Proxy p = new Proxy();
                    p.setHost( proxy.getHost() );
                    p.setProtocol( proxy.getProtocol() );
                    p.setPort( proxy.getPort() );
                    p.setNonProxyHosts( proxy.getNonProxyHosts() );
                    p.setUserName( proxy.getUsername() );
                    p.setPassword( proxy.getPassword() );

                    repository.setProxy( p );
                }
                else
                {
                    repository.setProxy( null );
                }
            }
        }
    }

    public MetadataResolutionResult resolveMetadata( final MetadataResolutionRequest request )
    {

        //      ArtifactResolutionResult collect( Set<Artifact> artifacts,
        //      Artifact originatingArtifact,
        //      Map managedVersions,
        //      ArtifactRepository localRepository,
        //      List<ArtifactRepository> remoteRepositories,
        //      ArtifactMetadataSource source,
        //      ArtifactFilter filter,
        //      List<ResolutionListener> listeners,
        //      List<ConflictResolver> conflictResolvers )
        //      ArtifactResolutionResult result = artifactCollector.

        return null;
    }

    public void retrieve( final ArtifactRepository repository, final File destination, final String remotePath,
                          final ArtifactTransferListener transferListener )
        throws ArtifactTransferFailedException, ArtifactDoesNotExistException
    {
        try
        {
            wagonManager.getRemoteFile( repository, destination, remotePath,
                                        TransferListenerAdapter.newAdapter( transferListener ),
                                        ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN, true );
        }
        catch ( final org.apache.maven.wagon.TransferFailedException e )
        {
            throw new ArtifactTransferFailedException( getMessage( e, "Error transferring artifact." ), e );
        }
        catch ( final org.apache.maven.wagon.ResourceDoesNotExistException e )
        {
            throw new ArtifactDoesNotExistException( getMessage( e, "Requested artifact does not exist." ), e );
        }
    }

    public void publish( final ArtifactRepository repository, final File source, final String remotePath,
                         final ArtifactTransferListener transferListener )
        throws ArtifactTransferFailedException
    {
        try
        {
            wagonManager.putRemoteFile( repository, source, remotePath,
                                        TransferListenerAdapter.newAdapter( transferListener ) );
        }
        catch ( final org.apache.maven.wagon.TransferFailedException e )
        {
            throw new ArtifactTransferFailedException( getMessage( e, "Error transferring artifact." ), e );
        }
    }

    //
    // Artifact Repository Creation
    //
    public ArtifactRepository buildArtifactRepository( final Repository repo )
        throws InvalidRepositoryException
    {
        if ( repo != null )
        {
            final String id = repo.getId();

            if ( StringUtils.isEmpty( id ) )
            {
                throw new InvalidRepositoryException( "Repository identifier missing", "" );
            }

            final String url = repo.getUrl();

            if ( StringUtils.isEmpty( url ) )
            {
                throw new InvalidRepositoryException( "URL missing for repository " + id, id );
            }

            final ArtifactRepositoryPolicy snapshots = buildArtifactRepositoryPolicy( repo.getSnapshots() );

            final ArtifactRepositoryPolicy releases = buildArtifactRepositoryPolicy( repo.getReleases() );

            return createArtifactRepository( id, url, layouts.get( repo.getLayout() ), snapshots, releases );
        }
        else
        {
            return null;
        }
    }

    private ArtifactRepository createRepository( final String url, final String repositoryId, final boolean releases,
                                                 final String releaseUpdates, final boolean snapshots,
                                                 final String snapshotUpdates, final String checksumPolicy )
    {
        final ArtifactRepositoryPolicy snapshotsPolicy =
            new ArtifactRepositoryPolicy( snapshots, snapshotUpdates, checksumPolicy );

        final ArtifactRepositoryPolicy releasesPolicy =
            new ArtifactRepositoryPolicy( releases, releaseUpdates, checksumPolicy );

        return createArtifactRepository( repositoryId, url, null, snapshotsPolicy, releasesPolicy );
    }

    public ArtifactRepository createArtifactRepository( final String repositoryId, final String url,
                                                        ArtifactRepositoryLayout repositoryLayout,
                                                        final ArtifactRepositoryPolicy snapshots,
                                                        final ArtifactRepositoryPolicy releases )
    {
        if ( repositoryLayout == null )
        {
            repositoryLayout = layouts.get( "default" );
        }

        final ArtifactRepository artifactRepository =
            artifactRepositoryFactory.createArtifactRepository( repositoryId, url, repositoryLayout, snapshots,
                                                                releases );

        return artifactRepository;
    }

    private static String getMessage( final Throwable error, final String def )
    {
        if ( error == null )
        {
            return def;
        }
        final String msg = error.getMessage();
        if ( StringUtils.isNotEmpty( msg ) )
        {
            return msg;
        }
        return getMessage( error.getCause(), def );
    }

}
