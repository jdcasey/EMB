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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.commonjava.emb.EMBException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component( role = ProjectManager.class )
public class ProjectManager
{

    private static final Logger LOGGER = Logger.getLogger( ProjectManager.class );

    @Requirement
    private RepositorySystem aetherRepositorySystem;

    @Requirement
    private org.apache.maven.repository.RepositorySystem mavenRepositorySystem;

    @Requirement
    private ProjectBuilder projectBuilder;

    @Requirement
    private DependencyGraphResolver dependencyGraphResolver;

    @Requirement
    private SessionManager sessionManager;

    public GraphTrackingState resolveProjectDependencies( final File rootPom, final DependencyResolverSession session,
                                                          final boolean includeModuleProjects )
        throws EMBException
    {
        List<MavenProject> projects;
        if ( includeModuleProjects )
        {
            projects = buildReactorProjectInstances( rootPom, session );
        }
        else
        {
            projects = Collections.singletonList( buildProjectInstance( rootPom, session ) );
        }

        dependencyGraphResolver.resolveGraph( sessionManager.getRepositorySystemSession( session ), projects );

        return session.getGraphTrackingState();
    }

    public List<MavenProject> buildReactorProjectInstances( final File rootPom, final DependencyResolverSession session )
        throws DependencyResolverException
    {
        final ProjectBuildingRequest pbr = sessionManager.getProjectBuildingRequest( session );

        try
        {
            final List<File> pomFiles = Collections.singletonList( rootPom );
            final List<ProjectBuildingResult> results = projectBuilder.build( pomFiles, true, pbr );

            final List<MavenProject> projects = new ArrayList<MavenProject>( results.size() );
            for ( final ProjectBuildingResult result : results )
            {
                final MavenProject project = result.getProject();
                project.setRemoteArtifactRepositories( session.getRemoteArtifactRepositories() );

                projects.add( project );
            }

            session.setReactorProjects( projects );
            addProjects( session, projects );

            return projects;
        }
        catch ( final ProjectBuildingException e )
        {
            // logger.error( "Failed to build MavenProject instances from POM files for sorting: " + e.getMessage(), e
            // );
            final List<ProjectBuildingResult> results = e.getResults();

            final StringBuilder sb = new StringBuilder();

            if ( results == null )
            {
                sb.append( "Cannot build reactor project instances for root-POM: " ).append( rootPom );

                final StringWriter sWriter = new StringWriter();
                final PrintWriter pWriter = new PrintWriter( sWriter );

                e.printStackTrace( pWriter );
                sb.append( "\n" ).append( sWriter );
            }
            else
            {
                int i = 0;
                for ( final ProjectBuildingResult result : results )
                {
                    final List<ModelProblem> problems = result.getProblems();
                    if ( problems != null && !problems.isEmpty() )
                    {
                        sb.append( "\n" ).append( result.getProjectId() );
                        for ( final ModelProblem problem : problems )
                        {
                            sb.append( "\n\t" )
                              .append( problem.getMessage() )
                              .append( "\n\t\t" )
                              .append( problem.getSource() )
                              .append( "@" )
                              .append( problem.getLineNumber() )
                              .append( ":" + problem.getColumnNumber() );

                            if ( problem.getException() != null )
                            {
                                final StringWriter sWriter = new StringWriter();
                                final PrintWriter pWriter = new PrintWriter( sWriter );

                                problem.getException().printStackTrace( pWriter );
                                sb.append( "\n" ).append( sWriter );
                            }

                            sb.append( ( ++i ) ).append( " " ).append( sb );
                        }
                    }
                }
            }

            throw new DependencyResolverException( "Failed to build project instance. \n\n%s", e, sb );
        }
    }

    private void addProjects( final DependencyResolverSession session, final MavenProject... projects )
    {
        if ( projects == null || projects.length == 0 )
        {
            return;
        }

        addProjects( session, Arrays.asList( projects ) );
    }

    private void addProjects( final DependencyResolverSession session, final List<MavenProject> projects )
    {
        final GraphTrackingState graphState = session.getGraphTrackingState();
        for ( final MavenProject project : projects )
        {
            final LinkedList<MavenProject> parentage = new LinkedList<MavenProject>();
            parentage.addFirst( project );

            MavenProject parent = project.getParent();
            while ( parent != null )
            {
                parentage.addFirst( parent );
                parent = parent.getParent();
            }

            MavenProject current = parentage.removeFirst();
            while ( !parentage.isEmpty() )
            {
                DependencyTrackingState depState = graphState.getDependencyState( current );

                if ( depState == null )
                {
                    final org.apache.maven.artifact.Artifact pomArtifact =
                        mavenRepositorySystem.createArtifact( current.getGroupId(), current.getArtifactId(),
                                                              current.getVersion(), "pom" );

                    final Artifact aetherPomArtifact = RepositoryUtils.toArtifact( pomArtifact );

                    depState = graphState.track( aetherPomArtifact );
                }

                depState.addParentTrail( parentage );

                if ( !parentage.isEmpty() )
                {
                    current = parentage.removeFirst();
                }
            }
        }
    }

    public MavenProject buildProjectInstance( final File pomFile, final DependencyResolverSession session )
        throws DependencyResolverException
    {
        final ProjectBuildingRequest pbr = sessionManager.getProjectBuildingRequest( session );

        try
        {
            final ProjectBuildingResult result = projectBuilder.build( pomFile, pbr );

            final MavenProject project = result.getProject();
            project.setRemoteArtifactRepositories( session.getRemoteArtifactRepositories() );

            addProjects( session, project );

            return project;
        }
        catch ( final ProjectBuildingException e )
        {
            // logger.error( "Failed to build MavenProject instances from POM files for sorting: " + e.getMessage(), e
            // );
            final List<ProjectBuildingResult> results = e.getResults();

            final StringBuilder sb = new StringBuilder();

            if ( results == null )
            {
                sb.append( "Cannot build project instance for: " ).append( pomFile );

                final StringWriter sWriter = new StringWriter();
                final PrintWriter pWriter = new PrintWriter( sWriter );

                e.printStackTrace( pWriter );
                sb.append( "\n" ).append( sWriter );
            }
            else
            {
                int i = 0;
                for ( final ProjectBuildingResult result : results )
                {
                    final List<ModelProblem> problems = result.getProblems();
                    for ( final ModelProblem problem : problems )
                    {
                        sb.append( problem.getMessage() )
                          .append( "\n\t" )
                          .append( problem.getSource() )
                          .append( "@" )
                          .append( problem.getLineNumber() )
                          .append( ":" + problem.getColumnNumber() );

                        if ( problem.getException() != null )
                        {
                            final StringWriter sWriter = new StringWriter();
                            final PrintWriter pWriter = new PrintWriter( sWriter );

                            problem.getException().printStackTrace( pWriter );
                            sb.append( "\n" ).append( sWriter );
                        }

                        sb.append( ( ++i ) ).append( " " ).append( sb );
                    }
                }
            }

            throw new DependencyResolverException( "Failed to build project instance. \n\n%s", e, sb );
        }
    }

    public MavenProject buildProjectInstance( final String groupId, final String artifactId, final String version,
                                              final DependencyResolverSession session )
        throws DependencyResolverException
    {
        final ProjectBuildingRequest req = sessionManager.getProjectBuildingRequest( session );

        try
        {
            final org.apache.maven.artifact.Artifact pomArtifact =
                mavenRepositorySystem.createArtifact( groupId, artifactId, version, "pom" );

            final Artifact aetherPomArtifact = RepositoryUtils.toArtifact( pomArtifact );

            final ArtifactRequest artifactRequest =
                new ArtifactRequest( aetherPomArtifact, sessionManager.getRemoteRepositories( session ), "project" );

            final ArtifactResult artifactResult =
                aetherRepositorySystem.resolveArtifact( req.getRepositorySession(), artifactRequest );

            final File pomFile = artifactResult.getArtifact().getFile();
            final ProjectBuildingResult result = projectBuilder.build( pomFile, req );

            final MavenProject project = result.getProject();
            project.setRemoteArtifactRepositories( session.getRemoteArtifactRepositories() );

            project.setFile( pomFile );

            addProjects( session, project );

            return project;
        }
        catch ( final ProjectBuildingException e )
        {
            // logger.error( "Failed to build MavenProject instances from POM files for sorting: " + e.getMessage(), e
            // );
            final List<ProjectBuildingResult> results = e.getResults();

            final StringBuilder sb = new StringBuilder();

            int i = 0;
            if ( results == null )
            {
                sb.append( "Cannot build project instance for: " )
                  .append( groupId )
                  .append( ':' )
                  .append( artifactId )
                  .append( ':' )
                  .append( version );

                final StringWriter sWriter = new StringWriter();
                final PrintWriter pWriter = new PrintWriter( sWriter );

                e.printStackTrace( pWriter );
                sb.append( "\n" ).append( sWriter );
            }
            else
            {
                for ( final ProjectBuildingResult result : results )
                {
                    final List<ModelProblem> problems = result.getProblems();
                    for ( final ModelProblem problem : problems )
                    {
                        sb.append( problem.getMessage() )
                          .append( "\n\t" )
                          .append( problem.getSource() )
                          .append( "@" )
                          .append( problem.getLineNumber() )
                          .append( ":" + problem.getColumnNumber() );

                        if ( problem.getException() != null )
                        {
                            final StringWriter sWriter = new StringWriter();
                            final PrintWriter pWriter = new PrintWriter( sWriter );

                            problem.getException().printStackTrace( pWriter );
                            sb.append( "\n" ).append( sWriter );
                        }

                        sb.append( ( ++i ) ).append( " " ).append( sb );
                    }
                }
            }

            throw new DependencyResolverException( "Failed to build project instance. \n\n%s", e, sb );
        }
        catch ( final ArtifactResolutionException e )
        {
            throw new DependencyResolverException( "Failed to resolve POM: %s:%s:%s\nReason: %s", e, groupId,
                                                   artifactId, version, e.getMessage() );
        }
    }

    public Set<String> retrieveReactorProjectIds( final File rootPom )
        throws DependencyResolverException
    {
        if ( LOGGER.isInfoEnabled() )
        {
            LOGGER.info( "Finding projectIds contained within reactor for: " + rootPom );
        }

        final Map<File, Model> models = new LinkedHashMap<File, Model>();
        readReactorModels( rootPom, rootPom, models );

        final Set<String> projectIds = new HashSet<String>( models.size() );
        for ( final Model model : models.values() )
        {
            String groupId = model.getGroupId();
            final String artifactId = model.getArtifactId();
            String version = model.getVersion();
            String packaging = model.getPackaging();

            if ( packaging == null )
            {
                packaging = "jar";
            }

            if ( groupId == null || version == null )
            {
                final Parent parent = model.getParent();
                if ( parent != null )
                {
                    if ( groupId == null )
                    {
                        groupId = parent.getGroupId();
                    }
                    if ( version == null )
                    {
                        version = parent.getVersion();
                    }
                }
                else
                {
                    LOGGER.warn( String.format( "Invalid POM: %s", model.getId() ) );
                    continue;
                }
            }

            final String key = ArtifactUtils.key( groupId, artifactId, version );
            if ( LOGGER.isInfoEnabled() )
            {
                LOGGER.info( "Found: " + key );
            }

            projectIds.add( key );
        }

        return projectIds;
    }

    private void readReactorModels( final File topPom, final File pom, final Map<File, Model> models )
        throws DependencyResolverException
    {
        final Model model = readModel( pom );
        models.put( pom, model );

        if ( model.getModules() != null && !model.getModules().isEmpty() )
        {
            final File basedir = pom.getParentFile();

            final List<File> moduleFiles = new ArrayList<File>();

            for ( String module : model.getModules() )
            {
                if ( StringUtils.isEmpty( module ) )
                {
                    continue;
                }

                module = module.replace( '\\', File.separatorChar ).replace( '/', File.separatorChar );

                File moduleFile = new File( basedir, module );

                if ( moduleFile.isDirectory() )
                {
                    moduleFile = new File( moduleFile, "pom.xml" );
                }

                if ( !moduleFile.isFile() )
                {
                    LOGGER.warn( String.format( "In reactor of: %s: Child module %s of %s does not exist.", topPom,
                                                moduleFile, pom ) );
                    continue;
                }

                if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
                {
                    // we don't canonicalize on unix to avoid interfering with symlinks
                    try
                    {
                        moduleFile = moduleFile.getCanonicalFile();
                    }
                    catch ( final IOException e )
                    {
                        moduleFile = moduleFile.getAbsoluteFile();
                    }
                }
                else
                {
                    moduleFile = new File( moduleFile.toURI().normalize() );
                }

                moduleFiles.add( moduleFile );
                readReactorModels( topPom, moduleFile, models );
            }
        }
    }

    private Model readModel( final File pom )
        throws DependencyResolverException
    {
        Reader reader = null;
        try
        {
            reader = ReaderFactory.newPlatformReader( pom );

            return new MavenXpp3Reader().read( reader, false );
        }
        catch ( final IOException e )
        {
            LOGGER.error( String.format( "Failed to read POM: %s.\nReason: %s", pom, e.getMessage() ), e );
            throw new DependencyResolverException( "Failed to read POM: %s. Reason: %s", e, pom.getAbsolutePath(),
                                                   e.getMessage() );
        }
        catch ( final XmlPullParserException e )
        {
            LOGGER.error( String.format( "Failed to read POM: %s.\nReason: %s", pom, e.getMessage() ), e );
            throw new DependencyResolverException( "Failed to read POM: %s. Reason: %s", e, pom.getAbsolutePath(),
                                                   e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( reader );
        }
    }

}
