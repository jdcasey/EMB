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

package org.commonjava.emb.version.resolve;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface DependencyResolverSession
{

    public static final String SESSION_KEY = "dependency-resolver-session";

    List<ArtifactRepository> getRemoteArtifactRepositories();

    DependencyResolverSession setRemoteArtifactRepositories( final List<ArtifactRepository> remoteArtifactRepositories );

    Repository[] getResolveRepositories();

    File getLocalRepositoryDirectory();

    List<RemoteRepository> getRemoteRepositories();

    DependencyResolverSession setRemoteRepositories( final List<RemoteRepository> remoteRepositories );

    ProjectBuildingRequest getProjectBuildingRequest();

    DependencyResolverSession setProjectBuildingRequest( final ProjectBuildingRequest projectBuildingRequest );

    File getWorkdir();

    RepositorySystemSession getRepositorySystemSession();

    DependencyResolverSession setRepositorySystemSession( final RepositorySystemSession repositorySystemSession );

    DependencyResolverSession addReactorProject( final MavenProject project );

    DependencyResolverSession setReactorProjects( final MavenProject... projects );

    DependencyResolverSession setReactorProjects( final Iterable<MavenProject> projects );

    Collection<MavenProject> getReactorProjects();

    File getReactorPom( final Artifact artifact );

    MavenProject getReactorProject( final Artifact artifact );

    GraphTrackingState getGraphTrackingState();

    DependencyResolverSession setGraphTrackingState( final GraphTrackingState graphState );

    DependencyResolverSession copy();

}