package org.commonjava.xaven.component.vfind;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Repository;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.xaven.conf.XavenLibrary;

import java.util.ArrayList;
import java.util.List;

@Component( role = VersionFinder.class )
public class VersionFinder
{
    @Requirement( hint = "vfind" )
    private XavenLibrary library;

    private RepositorySystem repositorySystem;

    public String findMaximumVersion( final String groupId, final String artifactId, final VersionRange range,
                                      final String type, final List<Repository> remoteRepositories )
        throws VersionFinderException
    {
        final List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>( remoteRepositories.size() );
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

        final Artifact artifact = repositorySystem.createArtifact( groupId, artifactId, range.toString(), type );

        final ArtifactResolutionRequest request =
            new ArtifactResolutionRequest().setArtifact( artifact )
                                           .setRemoteRepositories( repos )
                                           .setForceUpdate( true )
                                           .setResolveRoot( true )
                                           .setResolveTransitively( false );

        final ArtifactResolutionResult result = repositorySystem.resolve( request );

        return null;
    }

}
