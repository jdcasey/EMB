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

package org.commonjava.emb.version.resolve.testutil;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.EMBException;
import org.commonjava.emb.app.AbstractEMBApplication;
import org.commonjava.emb.boot.embed.EMBEmbedder;
import org.commonjava.emb.boot.embed.EMBEmbedderBuilder;
import org.commonjava.emb.boot.embed.EMBEmbeddingException;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.VersionProvider;
import org.commonjava.emb.version.resolve.DependencyResolverSession;
import org.commonjava.emb.version.resolve.ProjectManager;
import org.commonjava.emb.version.resolve.SimpleResolverSession;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component( role = TestFixture.class )
public final class TestFixture
    extends AbstractEMBApplication
{

    @Requirement
    private EMBEmbedder emb;

    @Requirement
    private ProjectBuilder projectBuilder;

    @Requirement
    private ProjectManager projectManager;

    private File localRepoDir;

    private File tempProjectDir;

    private ArtifactRepository localRepository;

    private static Repository rawRemoteRepo;

    private ArtifactRepository remoteRepository;

    private final Set<File> tempFiles = new LinkedHashSet<File>();

    private boolean debug = false;

    private static TestFixture fixture;

    private static int instances = 0;

    private TestFixture()
        throws EMBException, IOException
    {
        setupDebugLogging();

        initFiles();
    }

    public static void setupDebugLogging()
    {
        final Configurator log4jConfigurator = new Configurator()
        {
            @SuppressWarnings( "unchecked" )
            public void doConfigure( final URL notUsed, final LoggerRepository repo )
            {
                final ConsoleAppender appender = new ConsoleAppender( new SimpleLayout() );
                appender.setImmediateFlush( true );
                appender.setThreshold( Level.ALL );

                repo.getRootLogger().addAppender( appender );

                final Enumeration<Logger> loggers = repo.getCurrentLoggers();
                while ( loggers.hasMoreElements() )
                {
                    final Logger logger = loggers.nextElement();
                    logger.addAppender( appender );
                    logger.setLevel( Level.INFO );
                }
            }
        };

        log4jConfigurator.doConfigure( null, LogManager.getLoggerRepository() );
    }

    public static TestFixture getInstance()
        throws EMBException, IOException
    {
        if ( fixture == null )
        {
            fixture = new TestFixture();
            fixture.load();
            fixture.initObjects();
        }

        instances++;
        return fixture;
    }

    public void setDebug( final boolean debug )
    {
        this.debug = debug;
    }

    public MavenProject getTestProject( final String path )
        throws ProjectBuildingException, IOException
    {
        final File pom = getTestFile( "projects", path );

        final DefaultProjectBuildingRequest req = new DefaultProjectBuildingRequest();
        req.setLocalRepository( localRepository );

        final List<ArtifactRepository> repos = new ArrayList<ArtifactRepository>();
        repos.add( remoteRepository );

        req.setRemoteRepositories( repos );

        req.setSystemProperties( System.getProperties() );
        req.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
        req.setProcessPlugins( false );

        final ProjectBuildingResult result = projectBuilder.build( pom, req );
        return result.getProject();
    }

    public File getTestFile( final String basedir, final String subpath )
        throws IOException
    {
        return getTestFile( basedir, subpath, false );
    }

    public File getTestFile( final String basedir, final String subpath, final boolean copy )
        throws IOException
    {
        String path;
        if ( basedir == null )
        {
            path = subpath;
        }
        else if ( subpath == null )
        {
            path = basedir;
        }
        else
        {
            path = new File( basedir, subpath ).getPath();
        }

        final URL resource = Thread.currentThread().getContextClassLoader().getResource( path );
        if ( resource == null )
        {
            Assert.fail( "Cannot find test file: " + path );
        }

        File pom = new File( resource.getPath() );
        if ( copy )
        {
            final File pomCopy = new File( tempProjectDir, path );
            pomCopy.getParentFile().mkdirs();

            FileUtils.copyFile( pom, pomCopy );
            pom = pomCopy;
        }

        return pom;
    }

    public EMBEmbedder emb()
    {
        return emb;
    }

    public void shutdown()
        throws IOException
    {
        instances--;
        if ( instances < 1 )
        {
            if ( !debug )
            {
                for ( final File f : tempFiles )
                {
                    if ( f != null && f.exists() )
                    {
                        FileUtils.forceDelete( f );
                    }
                }

                tempFiles.clear();
            }
        }
    }

    public Repository externalRepository()
    {
        return rawRemoteRepo;
    }

    public DependencyResolverSession newSession( final MavenProject... projects )
        throws IOException
    {
        final File workdir = File.createTempFile( "test-meadin.", ".work" );
        workdir.delete();
        workdir.mkdirs();

        final DependencyResolverSession session = new SimpleResolverSession( workdir, localRepoDir, rawRemoteRepo );

        session.setRemoteArtifactRepositories( Collections.singletonList( remoteRepository ) );

        if ( projects != null && projects.length > 0 )
        {
            for ( final MavenProject project : projects )
            {
                session.addReactorProject( project );
            }
        }

        return session;
    }

    @Override
    protected void afterLoading()
        throws EMBException
    {
        super.afterLoading();
        try
        {
            initFiles();
        }
        catch ( final IOException e )
        {
            throw new EMBException( "Failed to initialize: %s", e, e.getMessage() );
        }
    }

    private void initFiles()
        throws EMBException, IOException
    {
        if ( localRepoDir == null || !localRepoDir.isDirectory() )
        {
            try
            {
                localRepoDir = createTempDir( "local-repository.", ".dir" );
            }
            catch ( final IOException e )
            {
                throw new EMBEmbeddingException( "Failed to create test local-repository directory.\nReason: %s", e,
                                                 e.getMessage() );
            }
        }

        if ( tempProjectDir == null || !tempProjectDir.isDirectory() )
        {
            try
            {
                tempProjectDir = createTempDir( "test-projects.", ".dir" );
            }
            catch ( final IOException e )
            {
                throw new EMBEmbeddingException( "Failed to create temporary projects directory.\nReason: %s", e,
                                                 e.getMessage() );
            }
        }

        rawRemoteRepo = new Repository();
        rawRemoteRepo.setId( "test" );
        rawRemoteRepo.setName( "Test Remote Repository" );

        try
        {
            rawRemoteRepo.setUrl( getTestFile( "test-repo", null, false ).toURI().toURL().toExternalForm() );
        }
        catch ( final MalformedURLException e )
        {
            throw new EMBEmbeddingException( "Failed to create test remote-repository instance.\nReason: %s", e,
                                             e.getMessage() );
        }
    }

    private void initObjects()
        throws EMBException
    {
        try
        {
            remoteRepository = emb.serviceManager().mavenRepositorySystem().buildArtifactRepository( rawRemoteRepo );
            localRepository = emb.serviceManager().mavenRepositorySystem().createLocalRepository( localRepoDir );
        }
        catch ( final InvalidRepositoryException e )
        {
            throw new EMBEmbeddingException( "Failed to create  repository instances. Reason: %s", e, e.getMessage() );
        }
    }

    public File createTempDir( final String prefix, final String suffix )
        throws IOException
    {
        final File dir = createTempFile( prefix, suffix );

        dir.delete();
        dir.mkdirs();

        return dir;
    }

    public File createTempFile( final String prefix, final String suffix )
        throws IOException
    {
        final File f = File.createTempFile( prefix, suffix );
        tempFiles.add( f );

        return f;
    }

    @Override
    protected void configureBuilder( final EMBEmbedderBuilder builder )
        throws EMBException
    {
        super.configureBuilder( builder );

        // show the versions of things loaded
        // enable classpath scanning to avoid the need to generate plexus component descriptors before testing.
        builder.withVersion( true ).withClassScanningEnabled( true );
    }

    public ProjectManager projectManager()
    {
        return projectManager;
    }

    @Override
    public String getId()
    {
        return "depsolv";
    }

    @Override
    public String getName()
    {
        return "Dependency-Resolver";
    }

    @Override
    protected VersionProvider getVersionProvider()
    {
        return new MavenPomVersionProvider( "org.commonjava.emb.components", "emb-dependency-resolver" );
    }

}
