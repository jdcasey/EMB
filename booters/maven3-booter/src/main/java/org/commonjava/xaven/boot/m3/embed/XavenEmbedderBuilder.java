package org.commonjava.xaven.boot.m3.embed;

import static org.commonjava.xaven.conf.XavenLibraries.loadLibraries;

import org.apache.log4j.Level;
import org.apache.maven.Maven;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.cli.PrintStreamLogger;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.settings.building.SettingsBuilder;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.commonjava.xaven.boot.m3.plexus.XavenContainerConfiguration;
import org.commonjava.xaven.boot.m3.services.XavenServiceManager;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.plexus.ComponentSelector;
import org.commonjava.xaven.plexus.InstanceRegistry;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

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

    private ClassLoader coreClassLoader;

    private Maven maven;

    private ModelProcessor modelProcessor;

    private MutablePlexusContainer container;

    private MavenExecutionRequestPopulator executionRequestPopulator;

    private SettingsBuilder settingsBuilder;

    private DefaultSecDispatcher securityDispatcher;

    private XavenServiceManager serviceManager;

    private transient String mavenHome;

    private transient boolean loggerAutoCreated = false;

    private XavenEmbedder embedder;

    private String[] debugLogHandles;

    private boolean modelProcessorProvided;

    private boolean mavenProvided;

    private boolean executionRequestPopulatorProvided;

    private boolean settingsBuilderProvided;

    private boolean securityDispatcherProvided;

    private boolean serviceManagerProvided;

    private boolean logHandlesConfigured;

    private boolean xavenConfigurationProvided;

    public synchronized XavenEmbedderBuilder withSettingsBuilder( final SettingsBuilder settingsBuilder )
    {
        this.settingsBuilder = settingsBuilder;
        settingsBuilderProvided = true;
        return this;
    }

    public synchronized SettingsBuilder settingsBuilder()
        throws XavenEmbeddingException
    {
        if ( settingsBuilder == null )
        {
            settingsBuilder = lookup( SettingsBuilder.class );
            settingsBuilderProvided = false;
        }
        return settingsBuilder;
    }

    public synchronized XavenEmbedderBuilder withSecurityDispatcher( final DefaultSecDispatcher securityDispatcher )
    {
        this.securityDispatcher = securityDispatcher;
        securityDispatcherProvided = true;
        return this;
    }

    public synchronized DefaultSecDispatcher securityDispatcher()
        throws XavenEmbeddingException
    {
        if ( securityDispatcher == null )
        {
            securityDispatcher = (DefaultSecDispatcher) lookup( SecDispatcher.class, "maven" );
            securityDispatcherProvided = false;
        }
        return securityDispatcher;
    }

    public synchronized XavenEmbedderBuilder withServiceManager( final XavenServiceManager serviceManager )
    {
        this.serviceManager = serviceManager;
        serviceManagerProvided = true;
        return this;
    }

    public synchronized XavenServiceManager serviceManager()
        throws XavenEmbeddingException
    {
        if ( serviceManager == null )
        {
            serviceManager = lookup( XavenServiceManager.class );
            serviceManagerProvided = true;
        }
        return serviceManager;
    }

    public synchronized XavenEmbedderBuilder withExecutionRequestPopulator(
                                                                            final MavenExecutionRequestPopulator executionRequestPopulator )
    {
        this.executionRequestPopulator = executionRequestPopulator;
        executionRequestPopulatorProvided = true;
        return this;
    }

    public synchronized MavenExecutionRequestPopulator executionRequestPopulator()
        throws XavenEmbeddingException
    {
        if ( executionRequestPopulator == null )
        {
            executionRequestPopulator = lookup( MavenExecutionRequestPopulator.class );
            executionRequestPopulatorProvided = false;
        }

        return executionRequestPopulator;
    }

    public synchronized XavenEmbedderBuilder withCoreClassLoader( final ClassLoader classLoader )
    {
        coreClassLoader = classLoader;
        return this;
    }

    public synchronized XavenEmbedderBuilder withClassWorld( final ClassWorld classWorld )
    {
        this.classWorld = classWorld;
        return this;
    }

    public synchronized ClassLoader coreClassLoader()
    {
        if ( coreClassLoader == null )
        {
            coreClassLoader = Thread.currentThread().getContextClassLoader();
        }

        return coreClassLoader;
    }

    public synchronized ClassWorld classWorld()
    {
        if ( classWorld == null )
        {
            classWorld = new ClassWorld( "plexus.core", coreClassLoader() );
        }

        return classWorld;
    }

    public synchronized XavenEmbedderBuilder withMaven( final Maven maven )
    {
        this.maven = maven;
        mavenProvided = true;
        return this;
    }

    public synchronized Maven maven()
        throws XavenEmbeddingException
    {
        if ( maven == null )
        {
            maven = lookup( Maven.class );
            mavenProvided = false;
        }
        return maven;
    }

    public synchronized XavenEmbedderBuilder withModelProcessor( final ModelProcessor modelProcessor )
    {
        this.modelProcessor = modelProcessor;
        modelProcessorProvided = true;
        return this;
    }

    public synchronized ModelProcessor modelProcessor()
        throws XavenEmbeddingException
    {
        if ( modelProcessor == null )
        {
            modelProcessor = lookup( ModelProcessor.class );
            modelProcessorProvided = false;
        }

        return modelProcessor;
    }

    private <T> T lookup( final Class<T> cls )
        throws XavenEmbeddingException
    {
        try
        {
            return container().lookup( cls );
        }
        catch ( final ComponentLookupException e )
        {
            throw new XavenEmbeddingException( "Failed to lookup component: {0}. Reason: {1}", e, cls.getName(),
                                               e.getMessage() );
        }
    }

    private <T> T lookup( final Class<T> cls, final String hint )
        throws XavenEmbeddingException
    {
        try
        {
            return container().lookup( cls, hint );
        }
        catch ( final ComponentLookupException e )
        {
            throw new XavenEmbeddingException( "Failed to lookup component: {0} with hint: {1}. Reason: {2}", e,
                                               cls.getName(), hint, e.getMessage() );
        }
    }

    public synchronized XavenEmbedderBuilder withContainer( final MutablePlexusContainer container )
    {
        this.container = container;
        resetContainer();

        return this;
    }

    public synchronized void resetContainer()
    {
        if ( !modelProcessorProvided )
        {
            modelProcessor = null;
        }
        if ( !executionRequestPopulatorProvided )
        {
            executionRequestPopulator = null;
        }
        if ( !settingsBuilderProvided )
        {
            settingsBuilder = null;
        }
        if ( !securityDispatcherProvided )
        {
            securityDispatcher = null;
        }
        if ( !serviceManagerProvided )
        {
            serviceManager = null;
        }
        if ( !mavenProvided )
        {
            maven = null;
        }
        if ( !xavenConfigurationProvided )
        {
            xavenConfiguration = null;
        }
        if ( container != null )
        {
            container = null;
        }
    }

    public synchronized MutablePlexusContainer container()
        throws XavenEmbeddingException
    {
        if ( container == null )
        {
            final ContainerConfiguration cc =
                new XavenContainerConfiguration( xavenConfiguration(), selector(), instanceRegistry() ).setClassWorld(
                                                                                                                       classWorld() )
                                                                                                       .setName(
                                                                                                                 "maven" );

            DefaultPlexusContainer c;
            try
            {
                c = new DefaultPlexusContainer( cc );
            }
            catch ( final PlexusContainerException e )
            {
                throw new XavenEmbeddingException( "Failed to initialize component container: {0}", e, e.getMessage() );
            }

            c.setLoggerManager( new MavenLoggerManager( logger ) );

            container = c;
        }

        return container;
    }

    public synchronized ComponentSelector selector()
    {
        return xavenConfiguration().getComponentSelector();
    }

    public synchronized InstanceRegistry instanceRegistry()
    {
        return xavenConfiguration().getInstanceRegistry();
    }

    public XavenEmbedderBuilder withXavenConfiguration( final XavenConfiguration config )
    {
        xavenConfiguration = config;
        xavenConfigurationProvided = true;
        return this;
    }

    public synchronized XavenConfiguration xavenConfiguration()
    {
        final String[] debugLogHandles = debugLogHandles();
        if ( !logHandlesConfigured && debugLogHandles != null )
        {
            for ( final String logHandle : debugLogHandles )
            {
                final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( logHandle );
                logger.setLevel( Level.DEBUG );
            }

            logHandlesConfigured = true;
        }

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
                loadLibraries( xavenConfiguration );

                if ( debugLogHandles != null
                    && Arrays.binarySearch( debugLogHandles, XavenConfiguration.STANDARD_LOG_HANDLE_CORE ) > -1 )
                {
                    XavenEmbedder.showXavenInfo( xavenConfiguration, standardOut() );
                }
            }
            catch ( final IOException e )
            {
                logger.error( "Failed to query context classloader for component-overrides files. Reason: "
                    + e.getMessage(), e );
            }

            xavenConfigurationProvided = false;
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

    protected synchronized XavenEmbedder createEmbedder()
        throws XavenEmbeddingException
    {
        return new XavenEmbedder( maven(), xavenConfiguration(), container(), settingsBuilder(),
                                  executionRequestPopulator(), securityDispatcher(), serviceManager(), standardOut(),
                                  logger(), shouldShowErrors(), showVersion() );
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
            embedder = createEmbedder();
        }

        return embedder;
    }

    public void withDebugLogHandles( final String[] debugLogHandles )
    {
        this.debugLogHandles = debugLogHandles;
        logHandlesConfigured = false;
    }

    public String[] debugLogHandles()
    {
        return debugLogHandles;
    }

}
