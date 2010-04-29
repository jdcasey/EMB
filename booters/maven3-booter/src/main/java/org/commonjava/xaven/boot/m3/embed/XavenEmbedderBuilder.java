package org.commonjava.xaven.boot.m3.embed;

import static org.commonjava.xaven.conf.XavenExtensions.getComponentOverrides;
import static org.commonjava.xaven.conf.XavenExtensions.loadExtensionConfigurations;

import org.apache.maven.Maven;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.cli.PrintStreamLogger;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.settings.building.SettingsBuilder;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.commonjava.xaven.boot.m3.plexus.XavenContainerConfiguration;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

public class XavenEmbedderBuilder
{

    private boolean showErrors = false;

    private boolean quiet = false;

    private boolean debug = false;

    private boolean showVersion = false;

    private PrintStream stdout = System.out;

    private PrintStream stderr = System.err;

    private InputStream stdin = System.in;

    private Logger logger;

    private File logFile;

    private XavenConfiguration xavenConfiguration;

    private ClassWorld classWorld;

    private Maven maven;

    private ModelProcessor modelProcessor;

    private MutablePlexusContainer container;

    private MavenExecutionRequestPopulator executionRequestPopulator;

    private SettingsBuilder settingsBuilder;

    private DefaultSecDispatcher securityDispatcher;

    private transient String mavenHome;

    private transient boolean loggerAutoCreated = false;

    private XavenEmbedder embedder;

    public XavenEmbedderBuilder withSettingsBuilder( final SettingsBuilder settingsBuilder )
    {
        this.settingsBuilder = settingsBuilder;
        return this;
    }

    public SettingsBuilder settingsBuilder()
    {
        return settingsBuilder;
    }

    public XavenEmbedderBuilder withSecurityDispatcher( final DefaultSecDispatcher securityDispatcher )
    {
        this.securityDispatcher = securityDispatcher;
        return this;
    }

    public DefaultSecDispatcher securityDispatcher()
    {
        return securityDispatcher;
    }

    public XavenEmbedderBuilder withExecutionRequestPopulator(
                                                               final MavenExecutionRequestPopulator executionRequestPopulator )
    {
        this.executionRequestPopulator = executionRequestPopulator;
        return this;
    }

    public MavenExecutionRequestPopulator executionRequestPopulator()
    {
        return executionRequestPopulator;
    }

    public XavenEmbedderBuilder withClassWorld( final ClassWorld classWorld )
    {
        this.classWorld = classWorld;
        return this;
    }

    public ClassWorld classWorld()
    {
        return classWorld;
    }

    public XavenEmbedderBuilder withMaven( final Maven maven )
    {
        this.maven = maven;
        return this;
    }

    public Maven maven()
    {
        return maven;
    }

    public XavenEmbedderBuilder withModelProcessor( final ModelProcessor modelProcessor )
    {
        this.modelProcessor = modelProcessor;
        return this;
    }

    public ModelProcessor modelProcessor()
    {
        return modelProcessor;
    }

    public XavenEmbedderBuilder withContainer( final MutablePlexusContainer container )
    {
        this.container = container;
        return this;
    }

    public MutablePlexusContainer container()
    {
        return container;
    }

    public XavenEmbedderBuilder withXavenConfiguration( final XavenConfiguration config )
    {
        xavenConfiguration = config;
        return this;
    }

    public synchronized XavenConfiguration xavenConfiguration()
    {
        if ( xavenConfiguration == null )
        {
            xavenConfiguration = new XavenConfiguration();

            if ( shouldShowDebug() )
            {
                xavenConfiguration.withDebug();
            }
            else
            {
                xavenConfiguration.withoutDebug();
            }

            try
            {
                xavenConfiguration.withComponentSelections( getComponentOverrides() );
                xavenConfiguration.withExtensionConfigurations( loadExtensionConfigurations( xavenConfiguration ) );
            }
            catch ( final IOException e )
            {
                logger.error( "Failed to query context classloader for component-overrides files. Reason: "
                    + e.getMessage(), e );
            }
        }

        return xavenConfiguration;
    }

    public XavenEmbedderBuilder withVersion( final boolean showVersion )
    {
        this.showVersion = showVersion;
        return this;
    }

    public boolean showVersion()
    {
        return showVersion;
    }

    public XavenEmbedderBuilder withLogFile( final File logFile )
    {
        this.logFile = logFile;
        return this;
    }

    public File logFile()
    {
        return logFile;
    }

    public XavenEmbedderBuilder withQuietMode( final boolean quiet )
    {
        this.quiet = quiet;
        return this;
    }

    public boolean shouldBeQuiet()
    {
        return quiet;
    }

    public XavenEmbedderBuilder withDebugMode( final boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public boolean shouldShowDebug()
    {
        return debug;
    }

    public XavenEmbedderBuilder withErrorMode( final boolean showErrors )
    {
        this.showErrors = showErrors;
        return this;
    }

    public boolean shouldShowErrors()
    {
        return showErrors;
    }

    public synchronized XavenEmbedderBuilder withStandardOut( final PrintStream stdout )
    {
        this.stdout = stdout;

        if ( loggerAutoCreated )
        {
            logger = null;
        }

        return this;
    }

    public PrintStream standardOut()
    {
        return stdout;
    }

    public XavenEmbedderBuilder withStandardErr( final PrintStream stderr )
    {
        this.stderr = stderr;
        return this;
    }

    public PrintStream standardErr()
    {
        return stderr;
    }

    public XavenEmbedderBuilder withStandardIn( final InputStream stdin )
    {
        this.stdin = stdin;
        return this;
    }

    public InputStream standardIn()
    {
        return stdin;
    }

    public XavenEmbedderBuilder withLogger( final Logger logger )
    {
        this.logger = logger;
        return this;
    }

    public synchronized Logger logger()
    {
        if ( logger == null )
        {
            logger = new PrintStreamLogger( stdout );
            loggerAutoCreated = true;
        }

        return logger;
    }

    public synchronized String mavenHome()
    {
        if ( mavenHome == null )
        {
            String mavenHome = System.getProperty( "maven.home" );

            if ( mavenHome != null )
            {
                try
                {
                    mavenHome = new File( mavenHome ).getCanonicalPath();
                }
                catch ( final IOException e )
                {
                    mavenHome = new File( mavenHome ).getAbsolutePath();
                }

                System.setProperty( "maven.home", mavenHome );
                this.mavenHome = mavenHome;
            }
        }

        return mavenHome;
    }

    protected synchronized void wireLogging()
    {
        if ( logFile() != null )
        {
            try
            {
                final PrintStream newOut = new PrintStream( logFile );
                withStandardOut( newOut );
            }
            catch ( final FileNotFoundException e )
            {
            }
        }

        logger();
    }

    protected synchronized void wireContainerAndComponents()
        throws ComponentLookupException, PlexusContainerException
    {
        if ( classWorld() == null )
        {
            withClassWorld( new ClassWorld( "plexus.core", Thread.currentThread().getContextClassLoader() ) );
        }

        PlexusContainer container = container();

        if ( container == null )
        {
            final ContainerConfiguration cc =
                new XavenContainerConfiguration( xavenConfiguration() ).setClassWorld( classWorld() ).setName( "maven" );

            final DefaultPlexusContainer c = new DefaultPlexusContainer( cc );
            c.setLoggerManager( new MavenLoggerManager( logger ) );

            container = c;
            withContainer( c );
        }

        if ( maven() == null )
        {
            withMaven( container.lookup( Maven.class ) );
        }

        if ( executionRequestPopulator() == null )
        {
            withExecutionRequestPopulator( container.lookup( MavenExecutionRequestPopulator.class ) );
        }

        if ( modelProcessor() == null )
        {
            withModelProcessor( container.lookup( ModelProcessor.class ) );
        }

        if ( settingsBuilder() == null )
        {
            withSettingsBuilder( container.lookup( SettingsBuilder.class ) );
        }

        if ( securityDispatcher() == null )
        {
            withSecurityDispatcher( (DefaultSecDispatcher) container.lookup( SecDispatcher.class, "maven" ) );
        }
    }

    protected XavenEmbedder createEmbedder()
    {
        return new XavenEmbedder( maven(), xavenConfiguration(), container(), settingsBuilder(),
                                  executionRequestPopulator(), securityDispatcher(), standardOut(), logger(),
                                  shouldShowErrors(), showVersion() );
    }

    public synchronized XavenEmbedder build()
        throws XavenEmbeddingException
    {
        if ( embedder == null )
        {
            logger();
            xavenConfiguration();
            mavenHome();

            wireLogging();
            try
            {
                wireContainerAndComponents();
            }
            catch ( final ComponentLookupException e )
            {
                throw new XavenEmbeddingException(
                                                   "Failed to initialize Plexus container and associated components: {0}",
                                                   e, e.getMessage() );
            }
            catch ( final PlexusContainerException e )
            {
                throw new XavenEmbeddingException(
                                                   "Failed to initialize Plexus container and associated components: {0}",
                                                   e, e.getMessage() );
            }

            embedder = createEmbedder();
        }

        return embedder;
    }

}
