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

package org.apache.maven.experimental.artifact.resolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DummyArtifactResolver
    implements ArtifactResolver, LogEnabled
{

    private Logger logger;

    @SuppressWarnings( "unchecked" )
    public void resolve( final Artifact artifact, final List remoteRepositories,
                         final ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        logDummy( "Resolving: {0}\nFrom Repositories:\n\n{1}\n\nLocal Repository: {2}", artifact,
                  join( remoteRepositories.iterator(), "\n" ), localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", artifact );
    }

    @SuppressWarnings( "unchecked" )
    public void resolveAlways( final Artifact artifact, final List remoteRepositories,
                               final ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        logDummy( "Resolving (ALWAYS): {0}\nFrom Repositories:\n\n{1}\n\nLocal Repository: {2}", artifact,
                  join( remoteRepositories.iterator(), "\n" ), localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", artifact );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final List remoteRepositories,
                                                         final ArtifactRepository localRepository,
                                                         final ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        logDummy(
                  "Resolving (TRANSITIVELY):\n\n{0}\n\nParent Artifact: {1}\nFrom Repositories:\n\n{2}\n\nLocal Repository: {3}",
                  join( artifacts.iterator(), "\n" ), originatingArtifact, join( remoteRepositories.iterator(), "\n" ),
                  localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", originatingArtifact );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final List remoteRepositories,
                                                         final ArtifactRepository localRepository,
                                                         final ArtifactMetadataSource source, final List listeners )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        logDummy(
                  "Resolving (TRANSITIVELY):\n\n{0}\n\nParent Artifact: {1}\nFrom Repositories:\n\n{2}\n\nLocal Repository: {3}",
                  join( artifacts.iterator(), "\n" ), originatingArtifact, join( remoteRepositories.iterator(), "\n" ),
                  localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", originatingArtifact );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final ArtifactRepository localRepository,
                                                         final List remoteRepositories,
                                                         final ArtifactMetadataSource source,
                                                         final ArtifactFilter filter )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        logDummy(
                  "Resolving (TRANSITIVELY):\n\n{0}\n\nParent Artifact: {1}\nFrom Repositories:\n\n{2}\n\nLocal Repository: {3}",
                  join( artifacts.iterator(), "\n" ), originatingArtifact, join( remoteRepositories.iterator(), "\n" ),
                  localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", originatingArtifact );
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactResolutionResult resolveTransitively( final Set artifacts, final Artifact originatingArtifact,
                                                         final Map managedVersions,
                                                         final ArtifactRepository localRepository,
                                                         final List remoteRepositories,
                                                         final ArtifactMetadataSource source )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        logDummy(
                  "Resolving (TRANSITIVELY):\n\n{0}\n\nParent Artifact: {1}\nFrom Repositories:\n\n{2}\n\nLocal Repository: {3}",
                  join( artifacts.iterator(), "\n" ), originatingArtifact, join( remoteRepositories.iterator(), "\n" ),
                  localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", originatingArtifact );
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
        logDummy(
                  "Resolving (TRANSITIVELY):\n\n{0}\n\nParent Artifact: {1}\nFrom Repositories:\n\n{2}\n\nLocal Repository: {3}",
                  join( artifacts.iterator(), "\n" ), originatingArtifact, join( remoteRepositories.iterator(), "\n" ),
                  localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", originatingArtifact );
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
        logDummy(
                  "Resolving (TRANSITIVELY):\n\n{0}\n\nParent Artifact: {1}\nFrom Repositories:\n\n{2}\n\nLocal Repository: {3}",
                  join( artifacts.iterator(), "\n" ), originatingArtifact, join( remoteRepositories.iterator(), "\n" ),
                  localRepository );
        throw new ArtifactResolutionException( "This is just a dummy resolver.", originatingArtifact );
    }

    public void enableLogging( final Logger logger )
    {
        this.logger = logger;
    }

    private void logDummy( final String message, final Object... params )
    {
        logger.info( "\n\n\n\n\n" + MessageFormat.format( message, params ) + "\n\n\n\n" );
    }

    private String join( final Iterator<?> iterator, String separator )
    {
        if ( separator == null )
        {
            separator = "";
        }
        final StringBuilder buf = new StringBuilder( 256 ); // Java default is 16, probably too small
        while ( iterator.hasNext() )
        {
            buf.append( iterator.next() );
            if ( iterator.hasNext() )
            {
                buf.append( separator );
            }
        }
        return buf.toString();
    }
}
