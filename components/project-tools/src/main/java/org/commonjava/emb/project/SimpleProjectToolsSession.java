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

import static org.apache.maven.artifact.ArtifactUtils.key;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.commonjava.emb.project.graph.DependencyGraph;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class SimpleProjectToolsSession
    implements ProjectToolsSession
{

    private final File workdir;

    private final Repository[] resolveRepositories;

    private DependencyGraph dependencyGraph;

    private transient List<ArtifactRepository> remoteArtifactRepositories;

    private transient List<RemoteRepository> remoteRepositories;

    private transient ProjectBuildingRequest projectBuildingRequest;

    private transient RepositorySystemSession repositorySystemSession;

    private transient LinkedHashMap<String, MavenProject> reactorProjects = new LinkedHashMap<String, MavenProject>();

    private final File localRepositoryDirectory;

    public SimpleProjectToolsSession( final File workdir, final Repository... resolveRepositories )
    {
        this( workdir, new File( workdir, "local-repository" ), resolveRepositories );
    }

    public SimpleProjectToolsSession( final File workdir, final File localRepositoryDirectory,
                                      final Repository... resolveRepositories )
    {
        this.workdir = workdir;
        this.localRepositoryDirectory = localRepositoryDirectory;
        this.resolveRepositories = resolveRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getRemoteArtifactRepositories()
     */
    @Override
    public List<ArtifactRepository> getRemoteArtifactRepositories()
    {
        return remoteArtifactRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#setRemoteArtifactRepositories(java.util.List)
     */
    @Override
    public ProjectToolsSession setRemoteArtifactRepositories( final List<ArtifactRepository> remoteArtifactRepositories )
    {
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getResolveRepositories()
     */
    @Override
    public Repository[] getResolveRepositories()
    {
        return resolveRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getRemoteRepositories()
     */
    @Override
    public List<RemoteRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getRemoteRepositoriesArray()
     */
    @Override
    public RemoteRepository[] getRemoteRepositoriesArray()
    {
        return remoteRepositories == null ? new RemoteRepository[] {}
                        : remoteRepositories.toArray( new RemoteRepository[] {} );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#setRemoteRepositories(java.util.List)
     */
    @Override
    public ProjectToolsSession setRemoteRepositories( final List<RemoteRepository> remoteRepositories )
    {
        this.remoteRepositories = remoteRepositories;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getProjectBuildingRequest()
     */
    @Override
    public ProjectBuildingRequest getProjectBuildingRequest()
    {
        return projectBuildingRequest;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#setProjectBuildingRequest(org.apache.maven.project.ProjectBuildingRequest)
     */
    @Override
    public ProjectToolsSession setProjectBuildingRequest( final ProjectBuildingRequest projectBuildingRequest )
    {
        this.projectBuildingRequest = projectBuildingRequest;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getWorkdir()
     */
    @Override
    public File getWorkdir()
    {
        return workdir;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getRepositorySystemSession()
     */
    @Override
    public RepositorySystemSession getRepositorySystemSession()
    {
        return repositorySystemSession;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#setRepositorySystemSession(org.sonatype.aether.RepositorySystemSession)
     */
    @Override
    public ProjectToolsSession setRepositorySystemSession( final RepositorySystemSession repositorySystemSession )
    {
        this.repositorySystemSession = repositorySystemSession;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#addReactorProject(org.apache.maven.project.MavenProject)
     */
    @Override
    public synchronized ProjectToolsSession addReactorProject( final MavenProject project )
    {
        final String id = key( project.getGroupId(), project.getArtifactId(), project.getVersion() );
        if ( !reactorProjects.containsKey( id ) )
        {
            reactorProjects.put( id, project );
        }

        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#setReactorProjects(org.apache.maven.project.MavenProject)
     */
    @Override
    public synchronized ProjectToolsSession setReactorProjects( final MavenProject... projects )
    {
        reactorProjects.clear();
        for ( final MavenProject project : projects )
        {
            addReactorProject( project );
        }

        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#setReactorProjects(java.lang.Iterable)
     */
    @Override
    public synchronized ProjectToolsSession setReactorProjects( final Iterable<MavenProject> projects )
    {
        reactorProjects.clear();
        for ( final MavenProject project : projects )
        {
            addReactorProject( project );
        }

        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getReactorProjects()
     */
    @Override
    public Collection<MavenProject> getReactorProjects()
    {
        return new ArrayList<MavenProject>( reactorProjects.values() );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getReactorPom(org.sonatype.aether.artifact.Artifact)
     */
    @Override
    public File getReactorPom( final Artifact artifact )
    {
        final MavenProject project = getReactorProject( artifact );
        return project == null ? null : project.getFile();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getReactorProject(org.sonatype.aether.artifact.Artifact)
     */
    @Override
    public MavenProject getReactorProject( final Artifact artifact )
    {
        final String id = key( artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion() );
        return reactorProjects.get( id );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getDependencyGraph()
     */
    @Override
    public synchronized DependencyGraph getDependencyGraph()
    {
        if ( dependencyGraph == null )
        {
            dependencyGraph = new DependencyGraph();
        }

        return dependencyGraph;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#setGraphTracker(org.commonjava.emb.project.graph.DependencyGraphTracker)
     */
    @Override
    public ProjectToolsSession setDependencyGraph( final DependencyGraph dependencyGraph )
    {
        this.dependencyGraph = dependencyGraph;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#copyOf(org.commonjava.emb.project.ProjectToolsSession)
     */
    @Override
    public ProjectToolsSession copy()
    {
        final SimpleProjectToolsSession copy = new SimpleProjectToolsSession( workdir, resolveRepositories );
        copy.dependencyGraph = dependencyGraph;

        copy.projectBuildingRequest =
            projectBuildingRequest == null ? null : new DefaultProjectBuildingRequest( projectBuildingRequest );

        copy.reactorProjects = new LinkedHashMap<String, MavenProject>( reactorProjects );

        copy.remoteArtifactRepositories =
            remoteArtifactRepositories == null ? null : new ArrayList<ArtifactRepository>( remoteArtifactRepositories );

        copy.remoteRepositories =
            remoteRepositories == null ? null : new ArrayList<RemoteRepository>( remoteRepositories );

        copy.repositorySystemSession =
            repositorySystemSession == null ? null : new DefaultRepositorySystemSession( repositorySystemSession );

        return copy;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.project.ProjectToolsSession#getLocalRepositoryDirectory()
     */
    @Override
    public File getLocalRepositoryDirectory()
    {
        return localRepositoryDirectory;
    }

}
