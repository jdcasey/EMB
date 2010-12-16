/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.project;

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

public interface ProjectToolsSession
{

    public static final String SESSION_KEY = "dependency-resolver-session";

    List<ArtifactRepository> getRemoteArtifactRepositories();

    ProjectToolsSession setRemoteArtifactRepositories( final List<ArtifactRepository> remoteArtifactRepositories );

    Repository[] getResolveRepositories();

    File getLocalRepositoryDirectory();

    List<RemoteRepository> getRemoteRepositories();

    ProjectToolsSession setRemoteRepositories( final List<RemoteRepository> remoteRepositories );

    ProjectBuildingRequest getProjectBuildingRequest();

    ProjectToolsSession setProjectBuildingRequest( final ProjectBuildingRequest projectBuildingRequest );

    File getWorkdir();

    RepositorySystemSession getRepositorySystemSession();

    ProjectToolsSession setRepositorySystemSession( final RepositorySystemSession repositorySystemSession );

    ProjectToolsSession addReactorProject( final MavenProject project );

    ProjectToolsSession setReactorProjects( final MavenProject... projects );

    ProjectToolsSession setReactorProjects( final Iterable<MavenProject> projects );

    Collection<MavenProject> getReactorProjects();

    File getReactorPom( final Artifact artifact );

    MavenProject getReactorProject( final Artifact artifact );

    DependencyGraphTracker getGraphTracker();

    ProjectToolsSession setGraphTracker( final DependencyGraphTracker graphState );

    ProjectToolsSession copy();

    RemoteRepository[] getRemoteRepositoriesArray();

}
