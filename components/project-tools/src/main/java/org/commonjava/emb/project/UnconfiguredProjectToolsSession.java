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

import static org.apache.commons.lang.StringUtils.join;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.mae.internal.container.VirtualInstance;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.commonjava.emb.project.graph.DependencyGraph;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Uninitialized / Uninitializable {@link ProjectToolsSession} implementation, mainly used as a 
 * placeholder for another intended {@link ProjectToolsSession} {@link VirtualInstance}. For all
 * methods, this implementation will throw an {@link IllegalStateException} indicating that some
 * method needs to be called which will set the <b>REAL</b> instance.
 * 
 * @author John Casey
 */
public class UnconfiguredProjectToolsSession
    implements ProjectToolsSession
{
    
    private final String message;
    
    public UnconfiguredProjectToolsSession( String...methodCalls )
    {
        this.message =
            "ProjectToolsSession has not been initialized! Please make sure one of {" + join( methodCalls, ", " )
                + "} has been called before trying to access this session component.";
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getRemoteArtifactRepositories()
     */
    @Override
    public List<ArtifactRepository> getRemoteArtifactRepositories()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setRemoteArtifactRepositories(java.util.List)
     */
    @Override
    public ProjectToolsSession setRemoteArtifactRepositories( List<ArtifactRepository> remoteArtifactRepositories )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getResolveRepositories()
     */
    @Override
    public Repository[] getResolveRepositories()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getLocalRepositoryDirectory()
     */
    @Override
    public File getLocalRepositoryDirectory()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getRemoteRepositories()
     */
    @Override
    public List<RemoteRepository> getRemoteRepositories()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setRemoteRepositories(java.util.List)
     */
    @Override
    public ProjectToolsSession setRemoteRepositories( List<RemoteRepository> remoteRepositories )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getProjectBuildingRequest()
     */
    @Override
    public ProjectBuildingRequest getProjectBuildingRequest()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setProjectBuildingRequest(org.apache.maven.project.ProjectBuildingRequest)
     */
    @Override
    public ProjectToolsSession setProjectBuildingRequest( ProjectBuildingRequest projectBuildingRequest )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getWorkdir()
     */
    @Override
    public File getWorkdir()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getRepositorySystemSession()
     */
    @Override
    public RepositorySystemSession getRepositorySystemSession()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setRepositorySystemSession(org.sonatype.aether.RepositorySystemSession)
     */
    @Override
    public ProjectToolsSession setRepositorySystemSession( RepositorySystemSession repositorySystemSession )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#addReactorProject(org.apache.maven.project.MavenProject)
     */
    @Override
    public ProjectToolsSession addReactorProject( MavenProject project )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setReactorProjects(org.apache.maven.project.MavenProject[])
     */
    @Override
    public ProjectToolsSession setReactorProjects( MavenProject... projects )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setReactorProjects(java.lang.Iterable)
     */
    @Override
    public ProjectToolsSession setReactorProjects( Iterable<MavenProject> projects )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getReactorProjects()
     */
    @Override
    public Collection<MavenProject> getReactorProjects()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getReactorPom(org.sonatype.aether.artifact.Artifact)
     */
    @Override
    public File getReactorPom( Artifact artifact )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getReactorProject(org.sonatype.aether.artifact.Artifact)
     */
    @Override
    public MavenProject getReactorProject( Artifact artifact )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getDependencyGraph()
     */
    @Override
    public DependencyGraph getDependencyGraph()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setDependencyGraph(org.commonjava.emb.project.graph.DependencyGraph)
     */
    @Override
    public ProjectToolsSession setDependencyGraph( DependencyGraph dependencyGraph )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#copy()
     */
    @Override
    public ProjectToolsSession copy()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getRemoteRepositoriesArray()
     */
    @Override
    public RemoteRepository[] getRemoteRepositoriesArray()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setExecutionRequest(org.apache.maven.execution.MavenExecutionRequest)
     */
    @Override
    public ProjectToolsSession setExecutionRequest( MavenExecutionRequest request )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getExecutionRequest()
     */
    @Override
    public MavenExecutionRequest getExecutionRequest()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setResolveThreads(int)
     */
    @Override
    public ProjectToolsSession setResolveThreads( int threads )
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#getResolveThreads()
     */
    @Override
    public int getResolveThreads()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#isProcessPomPlugins()
     */
    @Override
    public boolean isProcessPomPlugins()
    {
        throw new IllegalStateException( message );
    }

    /**
     * {@inheritDoc}
     * @see org.commonjava.emb.project.ProjectToolsSession#setProcessPomPlugins(boolean)
     */
    @Override
    public ProjectToolsSession setProcessPomPlugins( boolean resolvePlugins )
    {
        throw new IllegalStateException( message );
    }

}
