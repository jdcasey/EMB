package org.commonjava.xaven.component.vfind;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Repository;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.legacy.metadata.DefaultMetadataResolutionRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.xaven.component.vfind.conf.VersionFinderLibrary;
import org.commonjava.xaven.component.vscheme.SchemeAwareArtifactVersion;
import org.commonjava.xaven.component.vscheme.SchemeAwareVersionRange;
import org.commonjava.xaven.component.vscheme.VersionScheme;
import org.commonjava.xaven.component.vscheme.VersionSchemeSelector;
import org.commonjava.xaven.component.vscheme.XavenArtifactVersionException;
import org.commonjava.xaven.conf.XavenLibrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component( role = VersionFinder.class )
public class VersionFinder
{
    @SuppressWarnings( "unused" )
    @Requirement( hint = VersionFinderLibrary.KEY )
    private XavenLibrary library;

    @Requirement
    private RepositoryMetadataManager metadataManager;

    @Requirement
    private RepositorySystem repositorySystem;

    @Requirement
    private VersionSchemeSelector versionSchemeSelector;

    public String findMaximumMatchingVersion( final String groupId, final String artifactId, final String versionRange,
                                              final Repository... remoteRepositories )
        throws VersionFinderException, XavenArtifactVersionException
    {
        final VersionScheme scheme = versionSchemeSelector.getCurrentVersionScheme();
        final SchemeAwareVersionRange range = scheme.createRange( versionRange );

        return findMaximumMatchingVersion( groupId, artifactId, range, remoteRepositories );
    }

    public String findMaximumMatchingVersion( final String versionScheme, final String groupId,
                                              final String artifactId, final String versionRange,
                                              final Repository... remoteRepositories )
        throws VersionFinderException, XavenArtifactVersionException
    {
        final VersionScheme scheme = versionSchemeSelector.getVersionScheme( versionScheme );
        final SchemeAwareVersionRange range = scheme.createRange( versionRange );

        return findMaximumMatchingVersion( groupId, artifactId, range, remoteRepositories );
    }

    public String findMaximumMatchingVersion( final String groupId, final String artifactId, final VersionRange range,
                                              final Repository... remoteRepositories )
        throws VersionFinderException, XavenArtifactVersionException
    {
        return findMaximumMatchingVersion( null, groupId, artifactId, range, remoteRepositories );
    }

    public String findMaximumMatchingVersion( final String versionScheme, final String groupId,
                                              final String artifactId, final VersionRange range,
                                              final Repository... remoteRepositories )
        throws VersionFinderException, XavenArtifactVersionException
    {
        final VersionScheme scheme;
        if ( versionScheme == null )
        {
            scheme = versionSchemeSelector.getCurrentVersionScheme();
        }
        else
        {
            scheme = versionSchemeSelector.getVersionScheme( versionScheme );
        }

        return findMaximumMatchingVersion( groupId, artifactId, SchemeAwareVersionRange.from( range, scheme ),
                                           remoteRepositories );
    }

    public String findMaximumMatchingVersion( final String groupId, final String artifactId,
                                              final SchemeAwareVersionRange range,
                                              final Repository... remoteRepositories )
        throws VersionFinderException, XavenArtifactVersionException
    {
        final List<String> versions = retrieveMatchingVersions( groupId, artifactId, range, remoteRepositories );

        if ( versions == null || versions.isEmpty() )
        {
            return null;
        }

        final VersionScheme scheme = range.getVersionScheme();
        final Comparator<String> versionComparator = scheme.getVersionStringComparator();

        Collections.sort( versions, versionComparator );

        return versions.get( versions.size() - 1 );
    }

    protected List<String> retrieveMatchingVersions( final String groupId, final String artifactId,
                                                     final SchemeAwareVersionRange range,
                                                     final Repository... remoteRepositories )
        throws VersionFinderException, XavenArtifactVersionException
    {
        final List<ArtifactRepository> repos =
            new ArrayList<ArtifactRepository>( remoteRepositories == null ? 0 : remoteRepositories.length );

        if ( remoteRepositories != null )
        {
            for ( final Repository repository : remoteRepositories )
            {
                try
                {
                    repos.add( repositorySystem.buildArtifactRepository( repository ) );
                }
                catch ( final InvalidRepositoryException e )
                {
                    throw new VersionFinderException( "Failed to build ArtifactRepository from: %s\nReason: %s", e,
                                                      repository, e.getMessage() );
                }
            }
        }

        final Artifact artifact = repositorySystem.createArtifact( groupId, artifactId, range.toString(), "pom" );

        DefaultMetadataResolutionRequest req;
        try
        {
            req =
                new DefaultMetadataResolutionRequest().setArtifact( artifact )
                                                      .setForceUpdate( true )
                                                      .setRemoteRepositories( repos )
                                                      .setLocalRepository( repositorySystem.createDefaultLocalRepository() );
        }
        catch ( final InvalidRepositoryException e )
        {
            throw new VersionFinderException( "Failed to build LOCAL ArtifactRepository.\nReason: %s", e,
                                              e.getMessage() );
        }

        final ArtifactRepositoryMetadata arm = new ArtifactRepositoryMetadata( artifact );

        try
        {
            metadataManager.resolve( arm, req );
        }
        catch ( final RepositoryMetadataResolutionException e )
        {
            throw new VersionFinderException( "Failed to resolve available versions for: %s\nReason: %s", e, artifact,
                                              e.getMessage() );
        }

        if ( arm.getMetadata() != null && arm.getMetadata().getVersioning() != null )
        {
            final List<String> versions = arm.getMetadata().getVersioning().getVersions();
            if ( versions != null && !versions.isEmpty() )
            {
                final List<String> result = new ArrayList<String>( versions.size() );
                for ( final String version : versions )
                {
                    final ArtifactVersion v = new SchemeAwareArtifactVersion( version, range.getVersionScheme() );
                    if ( range.containsVersion( v ) )
                    {
                        result.add( version );
                    }
                }

                return result;
            }
        }

        return null;
    }
}
