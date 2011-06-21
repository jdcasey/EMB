/*
 *  Copyright (C) 2011 John Casey.
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

package org.commonjava.emb.project.session;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.mae.MAEException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.commonjava.emb.project.ProjectToolsException;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.util.List;

/**
 * 
 */
public interface SessionInjector
{

    ProjectBuildingRequest getProjectBuildingRequest( final ProjectToolsSession session )
        throws ProjectToolsException;

    RepositorySystemSession getRepositorySystemSession( final ProjectToolsSession session )
        throws MAEException;

    List<RemoteRepository> getRemoteRepositories( final ProjectToolsSession session )
        throws ProjectToolsException;

    List<ArtifactRepository> getArtifactRepositories( final ProjectToolsSession session )
        throws ProjectToolsException;

}