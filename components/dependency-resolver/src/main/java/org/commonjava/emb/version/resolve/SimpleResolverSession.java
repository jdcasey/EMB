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

import static org.apache.maven.artifact.ArtifactUtils.key;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class SimpleResolverSession
    implements DependencyResolverSession
{

    private final File workdir;

    private final Repository[] resolveRepositories;

    private GraphTrackingState graphState;

    private transient List<ArtifactRepository> remoteArtifactRepositories;

    private transient List<RemoteRepository> remoteRepositories;

    private transient ProjectBuildingRequest projectBuildingRequest;

    private transient RepositorySystemSession repositorySystemSession;

    private transient LinkedHashMap<String, MavenProject> reactorProjects = new LinkedHashMap<String, MavenProject>();

    private final File localRepositoryDirectory;

    public SimpleResolverSession( final File workdir, final Repository... resolveRepositories )
    {
        this( workdir, new File( workdir, "local-repository" ), resolveRepositories );
    }

    public SimpleResolverSession( final File workdir, final File localRepositoryDirectory,
                                  final Repository... resolveRepositories )
    {
        this.workdir = workdir;
        this.localRepositoryDirectory = localRepositoryDirectory;
        this.resolveRepositories = resolveRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getRemoteArtifactRepositories()
     */
    @Override
    public List<ArtifactRepository> getRemoteArtifactRepositories()
    {
        return remoteArtifactRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#setRemoteArtifactRepositories(java.util.List)
     */
    @Override
    public DependencyResolverSession setRemoteArtifactRepositories( final List<ArtifactRepository> remoteArtifactRepositories )
    {
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getResolveRepositories()
     */
    @Override
    public Repository[] getResolveRepositories()
    {
        return resolveRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getRemoteRepositories()
     */
    @Override
    public List<RemoteRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#setRemoteRepositories(java.util.List)
     */
    @Override
    public DependencyResolverSession setRemoteRepositories( final List<RemoteRepository> remoteRepositories )
    {
        this.remoteRepositories = remoteRepositories;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getProjectBuildingRequest()
     */
    @Override
    public ProjectBuildingRequest getProjectBuildingRequest()
    {
        return projectBuildingRequest;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#setProjectBuildingRequest(org.apache.maven.project.ProjectBuildingRequest)
     */
    @Override
    public DependencyResolverSession setProjectBuildingRequest( final ProjectBuildingRequest projectBuildingRequest )
    {
        this.projectBuildingRequest = projectBuildingRequest;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getWorkdir()
     */
    @Override
    public File getWorkdir()
    {
        return workdir;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getRepositorySystemSession()
     */
    @Override
    public RepositorySystemSession getRepositorySystemSession()
    {
        return repositorySystemSession;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#setRepositorySystemSession(org.sonatype.aether.RepositorySystemSession)
     */
    @Override
    public DependencyResolverSession setRepositorySystemSession( final RepositorySystemSession repositorySystemSession )
    {
        this.repositorySystemSession = repositorySystemSession;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#addReactorProject(org.apache.maven.project.MavenProject)
     */
    @Override
    public synchronized DependencyResolverSession addReactorProject( final MavenProject project )
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
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#setReactorProjects(org.apache.maven.project.MavenProject)
     */
    @Override
    public synchronized DependencyResolverSession setReactorProjects( final MavenProject... projects )
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
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#setReactorProjects(java.lang.Iterable)
     */
    @Override
    public synchronized DependencyResolverSession setReactorProjects( final Iterable<MavenProject> projects )
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
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getReactorProjects()
     */
    @Override
    public Collection<MavenProject> getReactorProjects()
    {
        return new ArrayList<MavenProject>( reactorProjects.values() );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getReactorPom(org.sonatype.aether.artifact.Artifact)
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
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getReactorProject(org.sonatype.aether.artifact.Artifact)
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
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getGraphTrackingState()
     */
    @Override
    public synchronized GraphTrackingState getGraphTrackingState()
    {
        if ( graphState == null )
        {
            graphState = new GraphTrackingState();
        }

        return graphState;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#setGraphTrackingState(org.commonjava.emb.version.resolve.GraphTrackingState)
     */
    @Override
    public DependencyResolverSession setGraphTrackingState( final GraphTrackingState graphState )
    {
        this.graphState = graphState;
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#copyOf(org.commonjava.emb.version.resolve.DependencyResolverSession)
     */
    @Override
    public DependencyResolverSession copy()
    {
        final SimpleResolverSession copy = new SimpleResolverSession( workdir, resolveRepositories );
        copy.graphState = graphState;

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
     * @see org.commonjava.emb.version.resolve.DependencyResolverSession#getLocalRepositoryDirectory()
     */
    @Override
    public File getLocalRepositoryDirectory()
    {
        return localRepositoryDirectory;
    }

}
