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

package org.commonjava.emb.resolve.redirect;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.wagon.TransferFailedException;

import java.io.IOException;
import java.util.List;

public class ResolutionException
    extends ArtifactResolutionException
{

    private static final long serialVersionUID = 1L;

    public ResolutionException( final String message, final Artifact artifact,
                                final List<ArtifactRepository> mirroredRepositories, final TransferFailedException e )
    {
        super( message, artifact, mirroredRepositories, e );
    }

    public ResolutionException( final String message, final Artifact artifact,
                                final List<ArtifactRepository> mirroredRepositories, final IOException e )
    {
        super( message, artifact, mirroredRepositories, e );
    }

    public ResolutionException( final String message, final Artifact resetArtifact,
                                final List<ArtifactRepository> remoteRepositories,
                                final ArtifactMetadataRetrievalException e )
    {
        super( message, resetArtifact, remoteRepositories, e );
    }

}
