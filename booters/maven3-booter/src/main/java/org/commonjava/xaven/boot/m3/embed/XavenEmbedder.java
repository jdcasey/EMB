package org.commonjava.xaven.boot.m3.embed;

import static org.commonjava.xaven.conf.XavenLibraries.loadLibraries;

import org.apache.maven.Maven;
import org.apache.maven.cli.CLIReportingUtils;
import org.apache.maven.exception.DefaultExceptionHandler;
import org.apache.maven.exception.ExceptionHandler;
import org.apache.maven.exception.ExceptionSummary;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulationException;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.properties.internal.EnvironmentUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.building.SettingsProblem;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.commonjava.xaven.XavenExecutionRequest;
import org.commonjava.xaven.boot.m3.log.EventLogger;
import org.commonjava.xaven.boot.m3.main.XavenMain;
import org.commonjava.xaven.boot.m3.services.XavenServiceManager;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.conf.mgmt.LoadOnFinish;
import org.commonjava.xaven.conf.mgmt.LoadOnStart;
import org.commonjava.xaven.conf.mgmt.XavenManagementException;
import org.commonjava.xaven.conf.mgmt.XavenManagementView;
import org.commonjava.xaven.plexus.ComponentKey;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

public class XavenEmbedder
{

    private static boolean xavenInfoShown;

    private final Logger logger;

    private final PrintStream standardOut;

    private final boolean shouldShowErrors;

    private final Maven maven;

    private final boolean showVersion;

    private final MutablePlexusContainer container;

    private final XavenConfiguration xavenConfiguration;

    private final SettingsBuilder settingsBuilder;

    private final MavenExecutionRequestPopulator executionRequestPopulator;

    private final DefaultSecDispatcher securityDispatcher;

    private transient final XavenServiceManager serviceManager;

    XavenEmbedder( final Maven maven, final XavenConfiguration xavenConfiguration,
                   final MutablePlexusContainer container, final SettingsBuilder settingsBuilder,
                   final MavenExecutionRequestPopulator executionRequestPopulator,
                   final DefaultSecDispatcher securityDispatcher, final XavenServiceManager serviceManager,
                   final PrintStream standardOut, final Logger logger, final boolean shouldShowErrors,
                   final boolean showVersion )
    {
        this.maven = maven;
        this.xavenConfiguration = xavenConfiguration;
        this.container = container;
        this.settingsBuilder = settingsBuilder;
        this.executionRequestPopulator = executionRequestPopulator;
        this.securityDispatcher = securityDispatcher;
        this.serviceManager = serviceManager;
        this.standardOut = standardOut;
        this.logger = logger;
        this.shouldShowErrors = shouldShowErrors;
        this.showVersion = showVersion;
    }

    public synchronized XavenServiceManager serviceManager()
        throws XavenEmbeddingException
    {
        return serviceManager;
    }

    public MavenExecutionResult execute( final XavenExecutionRequest request )
        throws XavenEmbeddingException
    {
        final PrintStream oldOut = System.out;
        try
        {
            if ( standardOut != null )
            {
                System.setOut( standardOut );
            }

            doExecutionStarting();

            injectEnvironment( request );
            printInfo( request );
            return maven.execute( request.asMavenExecutionRequest() );
        }
        finally
        {
            doExecutionFinished();
            System.setOut( oldOut );
        }
    }

    public String encryptMasterPassword( final XavenExecutionRequest request )
        throws XavenEmbeddingException
    {
        String passwd = request.getPasswordToEncyrpt();
        if ( passwd == null )
        {
            passwd = "";
        }

        try
        {
            final DefaultPlexusCipher cipher = new DefaultPlexusCipher();

            final String result = cipher.encryptAndDecorate( passwd, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION );
            logger.info( result );

            return result;
        }
        catch ( final PlexusCipherException e )
        {
            throw new XavenEmbeddingException( "Failed to encrypt master password: {0}", e, e.getMessage() );
        }
    }

    public String encryptPassword( final XavenExecutionRequest request )
        throws XavenEmbeddingException
    {
        final String passwd = request.getPasswordToEncyrpt();

        String configurationFile = securityDispatcher.getConfigurationFile();

        if ( configurationFile.startsWith( "~" ) )
        {
            configurationFile = System.getProperty( "user.home" ) + configurationFile.substring( 1 );
        }

        final String file = System.getProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, configurationFile );

        String master = null;

        try
        {
            final SettingsSecurity sec = SecUtil.read( file, true );
            if ( sec != null )
            {
                master = sec.getMaster();
            }

            if ( master == null )
            {
                throw new IllegalStateException( "Master password is not set in the setting security file: " + file );
            }

            final DefaultPlexusCipher cipher = new DefaultPlexusCipher();
            final String masterPasswd =
                cipher.decryptDecorated( master, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION );

            final String result = cipher.encryptAndDecorate( passwd, masterPasswd );
            logger.info( result );

            return result;
        }
        catch ( final PlexusCipherException e )
        {
            throw new XavenEmbeddingException( "Failed to encrypt password: {0}", e, e.getMessage() );
        }
        catch ( final SecDispatcherException e )
        {
            throw new XavenEmbeddingException( "Failed to encrypt password: {0}", e, e.getMessage() );
        }
    }

    protected void doExecutionStarting()
        throws XavenEmbeddingException
    {
        for ( final XavenLibrary library : xavenConfiguration.getLibraries().values() )
        {
            final Set<ComponentKey> components = library.getManagementComponents( LoadOnStart.class );
            if ( components != null && !components.isEmpty() )
            {
                final XavenManagementView mgmtView = new EmbedderManagementView( container, xavenConfiguration );
                for ( final ComponentKey key : components )
                {
                    try
                    {
                        final LoadOnStart los = (LoadOnStart) container.lookup( key.getRole(), key.getHint() );
                        los.executionStarting( mgmtView );
                    }
                    catch ( final ComponentLookupException e )
                    {
                        throw new XavenEmbeddingException(
                                                           "Failed to lookup load-on-start component for initialization: %s.\nReason: %s",
                                                           e, key, e.getMessage() );
                    }
                }
            }
        }
    }

    protected void doExecutionFinished()
    {
        for ( final XavenLibrary library : xavenConfiguration.getLibraries().values() )
        {
            final Set<ComponentKey> components = library.getManagementComponents( LoadOnFinish.class );
            if ( components != null && !components.isEmpty() )
            {
                final XavenManagementView mgmtView = new EmbedderManagementView( container, xavenConfiguration );
                for ( final ComponentKey key : components )
                {
                    try
                    {
                        final LoadOnFinish lof = (LoadOnFinish) container.lookup( key.getRole(), key.getHint() );
                        lof.executionFinished( mgmtView );
                    }
                    catch ( final ComponentLookupException e )
                    {
                        logger.error( String.format( "Failed to lookup load-on-start component for initialization: %s.\nReason: %s",
                                                     key, e.getMessage() ), e );
                    }
                }
            }
        }
    }

    protected synchronized void injectEnvironment( final XavenExecutionRequest request )
        throws XavenEmbeddingException
    {
        injectLogSettings( request );

        initializeXaven( request );

        injectProperties( request );

        injectSettings( request );

        injectFromProperties( request );
    }

    private void initializeXaven( final XavenExecutionRequest request )
    {
        xavenConfiguration.withXavenExecutionRequest( request );
        if ( request.isInteractiveMode() )
        {
            xavenConfiguration.interactive();
        }
        else
        {
            xavenConfiguration.nonInteractive();
        }

        if ( Logger.LEVEL_DEBUG == request.getLoggingLevel() )
        {
            xavenConfiguration.withDebug();
        }
        else
        {
            xavenConfiguration.withoutDebug();
        }
    }

    protected void injectFromProperties( final XavenExecutionRequest request )
    {
        String localRepoProperty = request.getUserProperties().getProperty( XavenMain.LOCAL_REPO_PROPERTY );

        if ( localRepoProperty == null )
        {
            localRepoProperty = request.getSystemProperties().getProperty( XavenMain.LOCAL_REPO_PROPERTY );
        }

        if ( localRepoProperty != null )
        {
            request.setLocalRepositoryPath( localRepoProperty );
        }
    }

    protected void injectLogSettings( final XavenExecutionRequest request )
    {
        final int logLevel = request.getLoggingLevel();

        if ( Logger.LEVEL_DEBUG == logLevel )
        {
            xavenConfiguration.withDebug();
        }
        else
        {
            xavenConfiguration.withoutDebug();
        }

        logger.setThreshold( logLevel );
        container.getLoggerManager().setThresholds( request.getLoggingLevel() );

        //        final Configurator log4jConfigurator = new Configurator()
        //        {
        //            @SuppressWarnings( "unchecked" )
        //            public void doConfigure( final URL notUsed, final LoggerRepository repo )
        //            {
        //                final Enumeration<org.apache.log4j.Logger> loggers = repo.getCurrentLoggers();
        //                while ( loggers.hasMoreElements() )
        //                {
        //                    final org.apache.log4j.Logger logger = loggers.nextElement();
        //                    if ( Logger.LEVEL_DEBUG == logLevel )
        //                    {
        //                        logger.setLevel( Level.DEBUG );
        //                    }
        //                    else if ( Logger.LEVEL_ERROR == logLevel )
        //                    {
        //                        logger.setLevel( Level.ERROR );
        //                    }
        //                }
        //            }
        //        };
        //
        //        log4jConfigurator.doConfigure( null, LogManager.getLoggerRepository() );

        request.setExecutionListener( new EventLogger( logger ) );
    }

    protected void injectProperties( final XavenExecutionRequest request )
    {
        final Properties systemProperties = new Properties();

        EnvironmentUtils.addEnvVars( systemProperties );
        systemProperties.putAll( System.getProperties() );

        if ( request.getSystemProperties() != null )
        {
            systemProperties.putAll( request.getSystemProperties() );
        }

        request.setSystemProperties( systemProperties );
    }

    protected void injectSettings( final XavenExecutionRequest request )
        throws XavenEmbeddingException
    {
        Settings settings = request.getSettings();
        SettingsBuildingResult settingsResult = null;
        if ( settings == null )
        {
            final SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
            settingsRequest.setGlobalSettingsFile( request.getGlobalSettingsFile() );
            settingsRequest.setUserSettingsFile( request.getUserSettingsFile() );

            settingsRequest.setSystemProperties( request.getSystemProperties() );
            settingsRequest.setUserProperties( request.getUserProperties() );

            try
            {
                settingsResult = settingsBuilder.build( settingsRequest );
            }
            catch ( final SettingsBuildingException e )
            {
                throw new XavenEmbeddingException(
                                                   "Failed to build settings; {0}\nGlobal settings: {1}\nUser settings: {2}",
                                                   e, e.getMessage(), request.getGlobalSettingsFile(),
                                                   request.getUserSettingsFile() );
            }

            settings = settingsResult.getEffectiveSettings();
        }

        try
        {
            executionRequestPopulator.populateFromSettings( request.asMavenExecutionRequest(), settings );
        }
        catch ( final MavenExecutionRequestPopulationException e )
        {
            throw new XavenEmbeddingException( "Failed to populate request from settings; {0}", e, e.getMessage() );
        }

        if ( !settingsResult.getProblems().isEmpty() && logger.isWarnEnabled() )
        {
            logger.warn( "" );
            logger.warn( "Some problems were encountered while building the effective settings" );

            for ( final SettingsProblem problem : settingsResult.getProblems() )
            {
                logger.warn( problem.getMessage() + " @ " + problem.getLocation() );
            }

            logger.warn( "" );
        }
    }

    public static void showXavenInfo( final XavenConfiguration xavenConfig, final PrintStream standardOut )
        throws IOException
    {
        if ( xavenInfoShown )
        {
            return;
        }

        standardOut.println();
        standardOut.println( "-- Xaven Libraries Loaded --" );
        standardOut.println();

        loadLibraries( xavenConfig );

        final Map<String, XavenLibrary> libraries = xavenConfig.getLibraries();
        for ( final XavenLibrary ext : libraries.values() )
        {
            standardOut.println( "+" + ext.getLabel() + " (Log handle: '" + ext.getLogHandle() + "')" );
        }

        standardOut.println();
        standardOut.println( "--------------------------" );
        standardOut.println();

        xavenInfoShown = true;
    }

    public static void showVersion( final XavenConfiguration xavenConfig, final PrintStream standardOut )
        throws IOException
    {
        showXavenInfo( xavenConfig, standardOut );
        CLIReportingUtils.showVersion( standardOut );
    }

    protected void printInfo( final XavenExecutionRequest request )
    {
        if ( Logger.LEVEL_DEBUG == request.getLoggingLevel() || showVersion )
        {
            try
            {
                showVersion( xavenConfiguration, standardOut );
            }
            catch ( final IOException e )
            {
                logger.error( "Failed to retrieve Xaven extension information: " + e.getMessage(), e );
            }
        }

        if ( shouldShowErrors )
        {
            logger.info( "Error stacktraces are turned on." );
        }

        if ( MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals( request.getGlobalChecksumPolicy() ) )
        {
            logger.info( "Disabling strict checksum verification on all artifact downloads." );
        }
        else if ( MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals( request.getGlobalChecksumPolicy() ) )
        {
            logger.info( "Enabling strict checksum verification on all artifact downloads." );
        }
    }

    public int formatErrorOutput( final XavenExecutionRequest request, final MavenExecutionResult result )
    {
        if ( result.hasExceptions() )
        {
            final ExceptionHandler handler = new DefaultExceptionHandler();

            final Map<String, String> references = new LinkedHashMap<String, String>();

            MavenProject project = null;

            for ( final Throwable exception : result.getExceptions() )
            {
                final ExceptionSummary summary = handler.handleException( exception );

                logSummary( summary, references, "", shouldShowErrors );

                if ( project == null && exception instanceof LifecycleExecutionException )
                {
                    project = ( (LifecycleExecutionException) exception ).getProject();
                }
            }

            logger.error( "" );

            if ( !shouldShowErrors )
            {
                logger.error( "To see the full stack trace of the errors, re-run Maven with the -e switch." );
            }
            if ( !logger.isDebugEnabled() )
            {
                logger.error( "Re-run Maven using the -X switch to enable full debug logging." );
            }

            if ( !references.isEmpty() )
            {
                logger.error( "" );
                logger.error( "For more information about the errors and possible solutions"
                    + ", please read the following articles:" );

                for ( final Map.Entry<String, String> entry : references.entrySet() )
                {
                    logger.error( entry.getValue() + " " + entry.getKey() );
                }
            }

            if ( project != null && !project.equals( result.getTopologicallySortedProjects().get( 0 ) ) )
            {
                logger.error( "" );
                logger.error( "After correcting the problems, you can resume the build with the command" );
                logger.error( "  mvn <goals> -rf :" + project.getArtifactId() );
            }

            if ( MavenExecutionRequest.REACTOR_FAIL_NEVER.equals( request.getReactorFailureBehavior() ) )
            {
                logger.info( "Build failures were ignored." );

                return 0;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            return 0;
        }
    }

    protected void logSummary( final ExceptionSummary summary, final Map<String, String> references, String indent,
                               final boolean showErrors )
    {
        String referenceKey = "";

        if ( StringUtils.isNotEmpty( summary.getReference() ) )
        {
            referenceKey = references.get( summary.getReference() );
            if ( referenceKey == null )
            {
                referenceKey = "[Help " + ( references.size() + 1 ) + "]";
                references.put( summary.getReference(), referenceKey );
            }
        }

        String msg = indent + summary.getMessage();

        if ( StringUtils.isNotEmpty( referenceKey ) )
        {
            if ( msg.indexOf( '\n' ) < 0 )
            {
                msg += " -> " + referenceKey;
            }
            else
            {
                msg += '\n' + indent + "-> " + referenceKey;
            }
        }

        if ( showErrors )
        {
            logger.error( msg, summary.getException() );
        }
        else
        {
            logger.error( msg );
        }

        indent += "  ";

        for ( final ExceptionSummary child : summary.getChildren() )
        {
            logSummary( child, references, indent, showErrors );
        }
    }

    private static final class EmbedderManagementView
        implements XavenManagementView
    {

        private final PlexusContainer container;

        private final XavenConfiguration configuration;

        EmbedderManagementView( final PlexusContainer container, final XavenConfiguration configuration )
        {
            this.container = container;
            this.configuration = configuration;
        }

        @Override
        public <T> T lookup( final Class<T> role, final String hint )
            throws XavenManagementException
        {
            try
            {
                return container.lookup( role, hint );
            }
            catch ( final ComponentLookupException e )
            {
                throw new XavenManagementException(
                                                    "Failed to lookup component for managed component.\nRole: %s\nHint: %s\nReason: %s",
                                                    e, role, hint, e.getMessage() );
            }
        }

        @Override
        public <T> T lookup( final Class<T> role )
            throws XavenManagementException
        {
            try
            {
                return container.lookup( role );
            }
            catch ( final ComponentLookupException e )
            {
                throw new XavenManagementException(
                                                    "Failed to lookup component for managed component.\nRole: %s\nHint: %s\nReason: %s",
                                                    e, role, PlexusConstants.PLEXUS_DEFAULT_HINT, e.getMessage() );
            }
        }

        @Override
        public XavenConfiguration getConfiguration()
        {
            return configuration;
        }

        @Override
        public <T> Map<String, T> lookupMap( final Class<T> role, final String... hints )
            throws XavenManagementException
        {
            try
            {
                return container.lookupMap( role, hints == null ? new ArrayList<String>() : Arrays.asList( hints ) );
            }
            catch ( final ComponentLookupException e )
            {
                throw new XavenManagementException(
                                                    "Failed to lookup component-map for managed component.\nRole: %s\nHints: %s\nReason: %s",
                                                    e, role, StringUtils.join( hints, "," ), e.getMessage() );
            }
        }

        @Override
        public <T> List<T> lookupList( final Class<T> role, final String... hints )
            throws XavenManagementException
        {
            try
            {
                return container.lookupList( role, hints == null ? new ArrayList<String>() : Arrays.asList( hints ) );
            }
            catch ( final ComponentLookupException e )
            {
                throw new XavenManagementException(
                                                    "Failed to lookup component-list for managed component.\nRole: %s\nHints: %s\nReason: %s",
                                                    e, role, StringUtils.join( hints, "," ), e.getMessage() );
            }
        }

    }

}
