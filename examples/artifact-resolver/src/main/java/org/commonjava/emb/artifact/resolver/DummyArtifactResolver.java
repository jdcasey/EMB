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

package org.commonjava.emb.artifact.resolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.wagon.events.TransferListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings( "deprecation" )
public class DummyArtifactResolver
    implements ArtifactResolver
{

    public ArtifactResolutionResult resolve( final ArtifactResolutionRequest request )
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    public void resolve( final Artifact artifact, final List<ArtifactRepository> remoteRepositories,
                         final ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    public void resolve( final Artifact artifact, final List<ArtifactRepository> remoteRepositories,
                         final ArtifactRepository localRepository, final TransferListener downloadMonitor )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    public void resolveAlways( final Artifact artifact, final List<ArtifactRepository> remoteRepositories,
                               final ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    public ArtifactResolutionResult resolveTransitively( final Set<Artifact> artifacts,
                                                         final Artifact originatingArtifact,
                                                         final List<ArtifactRepository> remoteRepositories,
                                                         final ArtifactRepository localRepository,
                                                         final ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    public ArtifactResolutionResult resolveTransitively( final Set<Artifact> artifacts,
                                                         final Artifact originatingArtifact,
                                                         final ArtifactRepository localRepository,
                                                         final List<ArtifactRepository> remoteRepositories,
                                                         final ArtifactMetadataSource source,
                                                         final ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set<Artifact> artifacts,
                                                         final Artifact originatingArtifact, final Map managedVersions,
                                                         final ArtifactRepository localRepository,
                                                         final List<ArtifactRepository> remoteRepositories,
                                                         final ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    public ArtifactResolutionResult resolveTransitively( final Set<Artifact> artifacts,
                                                         final Artifact originatingArtifact,
                                                         final List<ArtifactRepository> remoteRepositories,
                                                         final ArtifactRepository localRepository,
                                                         final ArtifactMetadataSource source,
                                                         final List<ResolutionListener> listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set<Artifact> artifacts,
                                                         final Artifact originatingArtifact, final Map managedVersions,
                                                         final ArtifactRepository localRepository,
                                                         final List<ArtifactRepository> remoteRepositories,
                                                         final ArtifactMetadataSource source,
                                                         final ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set<Artifact> artifacts,
                                                         final Artifact originatingArtifact, final Map managedVersions,
                                                         final ArtifactRepository localRepository,
                                                         final List<ArtifactRepository> remoteRepositories,
                                                         final ArtifactMetadataSource source,
                                                         final ArtifactFilter filter,
                                                         final List<ResolutionListener> listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        throw new UnsupportedOperationException( "Not Implemented." );
    }
}
