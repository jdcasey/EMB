package org.commonjava.emb.boot.embed;

import static org.commonjava.emb.conf.EMBLibraries.loadLibraries;

import org.apache.log4j.Level;
import org.apache.maven.Maven;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.cli.PrintStreamLogger;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.settings.building.SettingsBuilder;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.commonjava.emb.boot.services.EMBServiceManager;
import org.commonjava.emb.conf.CoreLibrary;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.conf.loader.EMBLibraryLoader;
import org.commonjava.emb.conf.loader.ServiceLibraryLoader;
import org.commonjava.emb.internal.plexus.EMBPlexusContainer;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;
import org.commonjava.emb.plexus.InstanceRegistry;
import org.commonjava.emb.plexus.VirtualInstance;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

public class EMBEmbedderBuilder
{

    private static final EMBLibraryLoader CORE_LOADER = new EMBLibraryLoader()
    {
        @Override
        public Collection<? extends EMBLibrary> loadLibraries( final EMBConfiguration embConfig )
            throws IOException
        {
            return Collections.singleton( new CoreLibrary() );
        }
    };

    private boolean showErrors = false;

    private boolean quiet = false;

    private boolean debug = false;

    private boolean showVersion = false;

    private PrintStream stdout = System.out;

    private PrintStream stderr = System.err;

    private InputStream stdin = System.in;

    private Logger logger;

    private File logFile;

    private EMBConfiguration embConfiguration;

    private ClassWorld classWorld;

    private ClassLoader coreClassLoader;

    private Maven maven;

    private ModelProcessor modelProcessor;

    private MutablePlexusContainer container;

    private MavenExecutionRequestPopulator executionRequestPopulator;

    private SettingsBuilder settingsBuilder;

    private DefaultSecDispatcher securityDispatcher;

    private EMBServiceManager serviceManager;

    private transient String mavenHome;

    private transient boolean loggerAutoCreated = false;

    private EMBEmbedder embedder;

    private String[] debugLogHandles;

    private boolean modelProcessorProvided;

    private boolean mavenProvided;

    private boolean executionRequestPopulatorProvided;

    private boolean settingsBuilderProvided;

    private boolean securityDispatcherProvided;

    private boolean serviceManagerProvided;

    private boolean logHandlesConfigured;

    private boolean embConfigurationProvided;

    private ContainerConfiguration containerConfiguration;

    private boolean classScanningEnabled;

    private List<EMBLibraryLoader> libraryLoaders;

    private final VirtualInstance<EMBEmbedder> embVirtual = new VirtualInstance<EMBEmbedder>( EMBEmbedder.class );

    public synchronized EMBEmbedderBuilder withSettingsBuilder( final SettingsBuilder settingsBuilder )
    {
        this.settingsBuilder = settingsBuilder;
        settingsBuilderProvided = true;
        return this;
    }

    public synchronized SettingsBuilder settingsBuilder()
        throws EMBEmbeddingException
    {
        if ( settingsBuilder == null )
        {
            settingsBuilder = lookup( SettingsBuilder.class );
            settingsBuilderProvided = false;
        }
        return settingsBuilder;
    }

    public synchronized EMBEmbedderBuilder withSecurityDispatcher( final DefaultSecDispatcher securityDispatcher )
    {
        this.securityDispatcher = securityDispatcher;
        securityDispatcherProvided = true;
        return this;
    }

    public synchronized DefaultSecDispatcher securityDispatcher()
        throws EMBEmbeddingException
    {
        if ( securityDispatcher == null )
        {
            securityDispatcher = (DefaultSecDispatcher) lookup( SecDispatcher.class, "maven" );
            securityDispatcherProvided = false;
        }
        return securityDispatcher;
    }

    public synchronized EMBEmbedderBuilder withServiceManager( final EMBServiceManager serviceManager )
    {
        this.serviceManager = serviceManager;
        serviceManagerProvided = true;
        return this;
    }

    public synchronized EMBServiceManager serviceManager()
        throws EMBEmbeddingException
    {
        if ( serviceManager == null )
        {
            serviceManager = lookup( EMBServiceManager.class );
            serviceManagerProvided = true;
        }
        return serviceManager;
    }

    public synchronized EMBEmbedderBuilder withExecutionRequestPopulator( final MavenExecutionRequestPopulator executionRequestPopulator )
    {
        this.executionRequestPopulator = executionRequestPopulator;
        executionRequestPopulatorProvided = true;
        return this;
    }

    public synchronized MavenExecutionRequestPopulator executionRequestPopulator()
        throws EMBEmbeddingException
    {
        if ( executionRequestPopulator == null )
        {
            executionRequestPopulator = lookup( MavenExecutionRequestPopulator.class );
            executionRequestPopulatorProvided = false;
        }

        return executionRequestPopulator;
    }

    public synchronized EMBEmbedderBuilder withCoreClassLoader( final ClassLoader classLoader )
    {
        coreClassLoader = classLoader;
        return this;
    }

    public synchronized EMBEmbedderBuilder withCoreClassLoader( final ClassLoader root, final Object... constituents )
        throws MalformedURLException
    {
        if ( constituents != null && constituents.length > 0 )
        {
            final Set<URL> urls = new LinkedHashSet<URL>();
            for ( final Object object : constituents )
            {
                if ( object instanceof URL )
                {
                    urls.add( (URL) object );
                }
                else if ( object instanceof CharSequence )
                {
                    urls.add( new URL( object.toString() ) );
                }
                else if ( object instanceof File )
                {
                    urls.add( ( (File) object ).toURI().toURL() );
                }
                else
                {
                    String fname;
                    ClassLoader cloader;
                    if ( object instanceof Class<?> )
                    {
                        fname = ( (Class<?>) object ).getName();
                        cloader = ( (Class<?>) object ).getClassLoader();
                    }
                    else
                    {
                        fname = object.getClass().getName();
                        cloader = object.getClass().getClassLoader();
                    }

                    fname = "/" + fname.replace( '.', '/' ) + ".class";

                    final URL resource = cloader.getResource( fname );
                    if ( resource == null )
                    {
                        throw new IllegalStateException( "Class doesn't appear in its own classloader! ["
                                        + object.getClass().getName() + "]" );
                    }

                    String path = resource.toExternalForm();
                    if ( path.startsWith( "jar:" ) )
                    {
                        path = path.substring( "jar:".length() );
                    }

                    final int idx = path.indexOf( '!' );
                    if ( idx > -1 )
                    {
                        path = path.substring( 0, idx );
                    }

                    urls.add( new URL( path ) );
                }
            }

            coreClassLoader = new URLClassLoader( urls.toArray( new URL[] {} ), root );
        }
        else
        {
            coreClassLoader = root;
        }

        return this;
    }

    public synchronized EMBEmbedderBuilder withClassWorld( final ClassWorld classWorld )
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

    public synchronized EMBEmbedderBuilder withContainerConfiguration( final ContainerConfiguration containerConfiguration )
    {
        this.containerConfiguration = containerConfiguration;
        return this;
    }

    public synchronized ContainerConfiguration containerConfiguration()
    {
        if ( containerConfiguration == null )
        {
            containerConfiguration =
                new DefaultContainerConfiguration().setClassWorld( classWorld() )
                                                   .setName( "maven" )
                                                   .setClassPathScanning( classScanningEnabled );
        }

        return containerConfiguration;
    }

    public synchronized EMBEmbedderBuilder withClassScanningEnabled( final boolean classScanningEnabled )
    {
        this.classScanningEnabled = classScanningEnabled;
        return this;
    }

    public boolean isClassScanningEnabled()
    {
        return classScanningEnabled;
    }

    public synchronized EMBEmbedderBuilder withMaven( final Maven maven )
    {
        this.maven = maven;
        mavenProvided = true;
        return this;
    }

    public synchronized Maven maven()
        throws EMBEmbeddingException
    {
        if ( maven == null )
        {
            maven = lookup( Maven.class );
            mavenProvided = false;
        }
        return maven;
    }

    public synchronized EMBEmbedderBuilder withModelProcessor( final ModelProcessor modelProcessor )
    {
        this.modelProcessor = modelProcessor;
        modelProcessorProvided = true;
        return this;
    }

    public synchronized ModelProcessor modelProcessor()
        throws EMBEmbeddingException
    {
        if ( modelProcessor == null )
        {
            modelProcessor = lookup( ModelProcessor.class );
            modelProcessorProvided = false;
        }

        return modelProcessor;
    }

    private <T> T lookup( final Class<T> cls )
        throws EMBEmbeddingException
    {
        try
        {
            return container().lookup( cls );
        }
        catch ( final ComponentLookupException e )
        {
            throw new EMBEmbeddingException( "Failed to lookup component: %s. Reason: %s", e, cls.getName(),
                                             e.getMessage() );
        }
    }

    private <T> T lookup( final Class<T> cls, final String hint )
        throws EMBEmbeddingException
    {
        try
        {
            return container().lookup( cls, hint );
        }
        catch ( final ComponentLookupException e )
        {
            throw new EMBEmbeddingException( "Failed to lookup component: {0} with hint: {1}. Reason: {2}", e,
                                             cls.getName(), hint, e.getMessage() );
        }
    }

    public synchronized EMBEmbedderBuilder withContainer( final MutablePlexusContainer container )
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
        if ( !embConfigurationProvided )
        {
            embConfiguration = null;
        }
        if ( container != null )
        {
            container = null;
        }
    }

    public synchronized MutablePlexusContainer container()
        throws EMBEmbeddingException
    {
        // Need to switch to using: org.codehaus.plexus.MutablePlexusContainer.addPlexusInjector(List<PlexusBeanModule>,
        // Module...)
        if ( container == null )
        {
            final ContainerConfiguration cc = containerConfiguration();

            final InstanceRegistry reg = new InstanceRegistry( instanceRegistry() );
            reg.addVirtual( new ComponentKey<EMBEmbedder>( EMBEmbedder.class ), embVirtual );

            EMBPlexusContainer c;
            try
            {
                c = new EMBPlexusContainer( cc, selector(), reg );
            }
            catch ( final PlexusContainerException e )
            {
                throw new EMBEmbeddingException( "Failed to initialize component container: {0}", e, e.getMessage() );
            }

            c.setLoggerManager( new MavenLoggerManager( logger ) );

            container = c;
        }

        return container;
    }

    public synchronized ComponentSelector selector()
    {
        return embConfiguration().getComponentSelector();
    }

    public synchronized InstanceRegistry instanceRegistry()
    {
        return embConfiguration().getInstanceRegistry();
    }

    public EMBEmbedderBuilder withEMBConfiguration( final EMBConfiguration config )
    {
        embConfiguration = config;
        embConfigurationProvided = true;
        return this;
    }

    public synchronized EMBConfiguration embConfiguration()
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

        if ( embConfiguration == null )
        {
            embConfiguration = new EMBConfiguration();

            if ( shouldShowDebug() )
            {
                embConfiguration.withDebug();
            }
            else
            {
                embConfiguration.withoutDebug();
            }

            try
            {
                final List<EMBLibraryLoader> loaders = libraryLoaders();
                final Collection<EMBLibrary> libraries = loadLibraries( embConfiguration, loaders );
                embConfiguration.withLibraries( libraries );

                if ( debugLogHandles != null
                                && Arrays.binarySearch( debugLogHandles, EMBConfiguration.STANDARD_LOG_HANDLE_CORE ) > -1 )
                {
                    EMBEmbedder.showEMBInfo( embConfiguration, loaders, standardOut() );
                }
            }
            catch ( final IOException e )
            {
                logger.error( "Failed to query context classloader for component-overrides files. Reason: "
                                              + e.getMessage(), e );
            }

            embConfigurationProvided = false;
        }

        return embConfiguration;
    }

    public EMBEmbedderBuilder withoutServiceLibraryLoader()
    {
        libraryLoaders = new ArrayList<EMBLibraryLoader>();

        return this;
    }

    public EMBEmbedderBuilder withLibraryLoader( final EMBLibraryLoader loader )
    {
        libraryLoadersInternal().add( loader );
        return this;
    }

    public EMBEmbedderBuilder withLibraryLoader( final EMBLibraryLoader loader, final int offset )
    {
        final List<EMBLibraryLoader> loaders = libraryLoadersInternal();
        if ( offset < 0 )
        {
            // idx is negative, so we can add to the size here to get the insert index.
            loaders.add( loaders.size() + offset, loader );
        }
        else
        {
            loaders.add( offset, loader );
        }

        return this;
    }

    private List<EMBLibraryLoader> libraryLoadersInternal()
    {
        if ( libraryLoaders == null )
        {
            libraryLoaders = new ArrayList<EMBLibraryLoader>( Collections.singletonList( new ServiceLibraryLoader() ) );
        }

        return libraryLoaders;
    }

    public List<EMBLibraryLoader> libraryLoaders()
    {
        final List<EMBLibraryLoader> loaders = libraryLoadersInternal();

        if ( !loaders.isEmpty() && CORE_LOADER != loaders.get( 0 ) )
        {
            loaders.remove( CORE_LOADER );
        }

        loaders.add( CORE_LOADER );

        return loaders;
    }

    public EMBEmbedderBuilder withVersion( final boolean showVersion )
    {
        this.showVersion = showVersion;
        return this;
    }

    public boolean showVersion()
    {
        return showVersion;
    }

    public EMBEmbedderBuilder withLogFile( final File logFile )
    {
        this.logFile = logFile;
        return this;
    }

    public File logFile()
    {
        return logFile;
    }

    public EMBEmbedderBuilder withQuietMode( final boolean quiet )
    {
        this.quiet = quiet;
        return this;
    }

    public boolean shouldBeQuiet()
    {
        return quiet;
    }

    public EMBEmbedderBuilder withDebugMode( final boolean debug )
    {
        this.debug = debug;
        return this;
    }

    public boolean shouldShowDebug()
    {
        return debug;
    }

    public EMBEmbedderBuilder withErrorMode( final boolean showErrors )
    {
        this.showErrors = showErrors;
        return this;
    }

    public boolean shouldShowErrors()
    {
        return showErrors;
    }

    public synchronized EMBEmbedderBuilder withStandardOut( final PrintStream stdout )
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

    public EMBEmbedderBuilder withStandardErr( final PrintStream stderr )
    {
        this.stderr = stderr;
        return this;
    }

    public PrintStream standardErr()
    {
        return stderr;
    }

    public EMBEmbedderBuilder withStandardIn( final InputStream stdin )
    {
        this.stdin = stdin;
        return this;
    }

    public InputStream standardIn()
    {
        return stdin;
    }

    public EMBEmbedderBuilder withLogger( final Logger logger )
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

    protected synchronized EMBEmbedder createEmbedder()
        throws EMBEmbeddingException
    {
        final EMBEmbedder embedder =
            new EMBEmbedder( maven(), embConfiguration(), container(), settingsBuilder(), executionRequestPopulator(),
                             securityDispatcher(), serviceManager(), libraryLoaders(), standardOut(), logger(),
                             shouldShowErrors(), showVersion() );

        embVirtual.setInstance( embedder );

        return embedder;
    }

    public synchronized EMBEmbedder build()
        throws EMBEmbeddingException
    {
        if ( embedder == null )
        {
            logger();
            embConfiguration();
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
