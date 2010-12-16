/*
 * Copyright (c) 2010 Red Hat, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see 
 * <http://www.gnu.org/licenses>.
 */

package org.commonjava.emb.version.resolve;

import org.apache.maven.project.MavenProject;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SessionWorkspaceReader
    implements WorkspaceReader
{

    private static final WorkspaceRepository REPO = new WorkspaceRepository();

    private final DependencyResolverSession session;

    public SessionWorkspaceReader( final DependencyResolverSession session )
    {
        this.session = session;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.repository.WorkspaceReader#getRepository()
     */
    @Override
    public WorkspaceRepository getRepository()
    {
        return REPO;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.repository.WorkspaceReader#findArtifact(org.sonatype.aether.artifact.Artifact)
     */
    @Override
    public File findArtifact( final Artifact artifact )
    {
        return session.getReactorPom( artifact );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.repository.WorkspaceReader#findVersions(org.sonatype.aether.artifact.Artifact)
     */
    @Override
    public List<String> findVersions( final Artifact artifact )
    {
        final List<String> versions = new ArrayList<String>( 1 );

        final MavenProject project = session.getReactorProject( artifact );
        if ( project != null )
        {
            versions.add( project.getVersion() );
        }

        return versions;
    }

}
