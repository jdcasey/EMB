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

package org.commonjava.emb.project;

import org.apache.maven.mae.MAEException;
import org.apache.maven.project.MavenProject;
import org.commonjava.emb.project.depgraph.DependencyGraph;
import org.commonjava.emb.project.session.ProjectToolsSession;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface ProjectLoader
{

    DependencyGraph loadProjectDependencyGraph( final File rootPom, final ProjectToolsSession session,
                                                final boolean includeModuleProjects )
        throws MAEException;

    DependencyGraph resolveProjectDependencies( final File rootPom, final ProjectToolsSession session,
                                                final boolean includeModuleProjects )
        throws MAEException;

    List<MavenProject> buildReactorProjectInstances( final ProjectToolsSession session, final File... rootPoms )
        throws ProjectToolsException;

    MavenProject buildProjectInstance( final File pomFile, final ProjectToolsSession session )
        throws ProjectToolsException;

    MavenProject buildProjectInstance( final String groupId, final String artifactId, final String version,
                                       final ProjectToolsSession session )
        throws ProjectToolsException;

    Set<String> retrieveReactorProjectIds( final File rootPom )
        throws ProjectToolsException;

}