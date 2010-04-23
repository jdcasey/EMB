package org.commonjava.xaven.boot.m3.main;

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

import static org.commonjava.xaven.conf.XavenExtensions.getComponentOverrides;
import static org.commonjava.xaven.conf.XavenExtensions.getLoadedExtensions;
import static org.commonjava.xaven.conf.XavenExtensions.loadExtensionConfigurations;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.maven.Maven;
import org.apache.maven.cli.CLIManager;
import org.apache.maven.cli.CLIReportingUtils;
import org.apache.maven.cli.MavenLoggerManager;
import org.apache.maven.cli.PrintStreamLogger;
import org.apache.maven.exception.DefaultExceptionHandler;
import org.apache.maven.exception.ExceptionHandler;
import org.apache.maven.exception.ExceptionSummary;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.lifecycle.internal.LifecycleWeaveBuilder;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.properties.internal.EnvironmentUtils;
import org.apache.maven.repository.ArtifactTransferListener;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.apache.maven.settings.building.SettingsProblem;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.StringUtils;
import org.commonjava.xaven.boot.m3.log.BatchTransferListener;
import org.commonjava.xaven.boot.m3.log.EventLogger;
import org.commonjava.xaven.boot.m3.log.InteractiveTransferListener;
import org.commonjava.xaven.boot.m3.plexus.XavenContainerConfiguration;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

public class XavenMain
{
    public static final String LOCAL_REPO_PROPERTY = "maven.repo.local";

    public static final String THREADS_DEPRECATED = "maven.threads.experimental";

    public static final String userHome = System.getProperty( "user.home" );

    public static final File userMavenConfigurationHome = new File( userHome, ".m2" );

    public static final File DEFAULT_USER_SETTINGS_FILE = new File( userMavenConfigurationHome, "settings.xml" );

    public static final File CONFIGURATION_DIRECTORY = userMavenConfigurationHome;

    public static final File DEFAULT_GLOBAL_SETTINGS_FILE =
        new File( System.getProperty( "maven.home", System.getProperty( "user.dir", "" ) ), "conf/settings.xml" );

    public static final File DEFAULT_USER_TOOLCHAINS_FILE = new File( userMavenConfigurationHome, "toolchains.xml" );

    private final ClassWorld classWorld;

    // Per-instance container supports fast embedded execution of core ITs
    private DefaultPlexusContainer container;

    private PrintStreamLogger logger;

    private ModelProcessor modelProcessor;

    private Maven maven;

    private MavenExecutionRequestPopulator executionRequestPopulator;

    private SettingsBuilder settingsBuilder;

    private DefaultSecDispatcher dispatcher;

    public XavenMain()
    {
        this( null );
    }

    // This supports painless invocation by the Verifier during embedded execution of the core ITs
    public XavenMain( final ClassWorld classWorld )
    {
        this.classWorld = classWorld;
    }

    public static void main( final String[] args )
    {
        final int result = main( args, null );

        System.exit( result );
    }

    /** @noinspection ConfusingMainMethod */
    public static int main( final String[] args, final ClassWorld classWorld )
    {
        final XavenMain cli = new XavenMain();
        return cli.doMain( new CliRequest( args, classWorld ) );
    }

    // TODO: need to externalize CliRequest
    public static int doMain( final String[] args, final ClassWorld classWorld )
    {
        final XavenMain cli = new XavenMain();
        return cli.doMain( new CliRequest( args, classWorld ) );
    }

    // This supports painless invocation by the Verifier during embedded execution of the core ITs
    public int doMain( final String[] args, final String workingDirectory, final PrintStream stdout,
                       final PrintStream stderr )
    {
        final CliRequest cliRequest = new CliRequest( args, classWorld );
        cliRequest.workingDirectory = workingDirectory;
        cliRequest.stdout = stdout;
        cliRequest.stderr = stderr;

        return doMain( cliRequest );
    }

    // TODO: need to externalize CliRequest
    public int doMain( final CliRequest cliRequest )
    {
        try
        {
            initialize( cliRequest );
            // Need to process cli options first to get possible logging options
            cli( cliRequest );
            logging( cliRequest );
            commands( cliRequest );
            properties( cliRequest );
            xavenConfiguration( cliRequest );
            container( cliRequest );
            settings( cliRequest );
            populateRequest( cliRequest );
            encryption( cliRequest );
            return execute( cliRequest );
        }
        catch ( final ExitException e )
        {
            return e.exitCode;
        }
        catch ( final Exception e )
        {
            CLIReportingUtils.showError( logger, "Error executing Maven.", e, cliRequest.showErrors );

            return 1;
        }
        finally
        {
            if ( cliRequest.fileStream != null )
            {
                cliRequest.fileStream.close();
            }
        }
    }

    protected void initialize( final CliRequest cliRequest )
    {
        if ( cliRequest.stdout == null )
        {
            cliRequest.stdout = System.out;
        }
        if ( cliRequest.stderr == null )
        {
            cliRequest.stderr = System.err;
        }

        if ( logger == null )
        {
            logger = new PrintStreamLogger( cliRequest.stdout );
        }
        else
        {
            logger.setStream( cliRequest.stdout );
        }

        if ( cliRequest.workingDirectory == null )
        {
            cliRequest.workingDirectory = System.getProperty( "user.dir" );
        }

        //
        // Make sure the Maven home directory is an absolute path to save us from confusion with say drive-relative
        // Windows paths.
        //
        final String mavenHome = System.getProperty( "maven.home" );

        if ( mavenHome != null )
        {
            System.setProperty( "maven.home", new File( mavenHome ).getAbsolutePath() );
        }
    }

    //
    // Logging needs to be handled in a standard way at the container level.
    //
    protected void logging( final CliRequest cliRequest )
    {
        cliRequest.debug = cliRequest.commandLine.hasOption( CLIManager.DEBUG );
        cliRequest.quiet = !cliRequest.debug && cliRequest.commandLine.hasOption( CLIManager.QUIET );
        cliRequest.showErrors = cliRequest.debug || cliRequest.commandLine.hasOption( CLIManager.ERRORS );

        if ( cliRequest.debug )
        {
            cliRequest.request.setLoggingLevel( MavenExecutionRequest.LOGGING_LEVEL_DEBUG );
        }
        else if ( cliRequest.quiet )
        {
            // TODO: we need to do some more work here. Some plugins use sys out or log errors at info level.
            // Ideally, we could use Warn across the board
            cliRequest.request.setLoggingLevel( MavenExecutionRequest.LOGGING_LEVEL_ERROR );
            // TODO:Additionally, we can't change the mojo level because the component key includes the version and it isn't known ahead of time. This seems worth changing.
        }
        else
        {
            cliRequest.request.setLoggingLevel( MavenExecutionRequest.LOGGING_LEVEL_INFO );
        }

        logger.setThreshold( cliRequest.request.getLoggingLevel() );

        if ( cliRequest.commandLine.hasOption( CLIManager.LOG_FILE ) )
        {
            File logFile = new File( cliRequest.commandLine.getOptionValue( CLIManager.LOG_FILE ) );
            logFile = resolveFile( logFile, cliRequest.workingDirectory );

            try
            {
                cliRequest.fileStream = new PrintStream( logFile );
                logger.setStream( cliRequest.fileStream );
            }
            catch ( final FileNotFoundException e )
            {
                cliRequest.stderr.println( e );
                logger.setStream( cliRequest.stdout );
            }
        }
        else
        {
            logger.setStream( cliRequest.stdout );
        }

        final Configurator log4jConfigurator = new Configurator()
        {
            @SuppressWarnings( "unchecked" )
            public void doConfigure( final URL notUsed, final LoggerRepository repo )
            {
                final Enumeration<Logger> loggers = repo.getCurrentLoggers();
                while ( loggers.hasMoreElements() )
                {
                    final Logger logger = loggers.nextElement();
                    if ( cliRequest.debug )
                    {
                        logger.setLevel( Level.DEBUG );
                    }
                }
            }
        };

        log4jConfigurator.doConfigure( null, LogManager.getLoggerRepository() );

        cliRequest.request.setExecutionListener( new EventLogger( logger ) );
    }

    //
    // Every bit of information taken from the CLI should be processed here.
    //
    protected void cli( final CliRequest cliRequest )
        throws Exception
    {
        final CLIManager cliManager = new CLIManager();

        try
        {
            cliRequest.commandLine = cliManager.parse( cliRequest.args );
        }
        catch ( final ParseException e )
        {
            cliRequest.stderr.println( "Unable to parse command line options: " + e.getMessage() );
            cliManager.displayHelp( cliRequest.stdout );
            throw e;
        }

        // TODO: these should be moved out of here. Wrong place.
        //
        if ( cliRequest.commandLine.hasOption( CLIManager.HELP ) )
        {
            cliManager.displayHelp( cliRequest.stdout );
            throw new ExitException( 0 );
        }

        if ( cliRequest.commandLine.hasOption( CLIManager.VERSION ) )
        {
            showXavenInfo( cliRequest );
            CLIReportingUtils.showVersion( cliRequest.stdout );
            throw new ExitException( 0 );
        }
    }

    protected void commands( final CliRequest cliRequest )
    {
        if ( cliRequest.debug || cliRequest.commandLine.hasOption( CLIManager.SHOW_VERSION ) )
        {
            showXavenInfo( cliRequest );
            CLIReportingUtils.showVersion( cliRequest.stdout );
        }

        if ( cliRequest.showErrors )
        {
            logger.info( "Error stacktraces are turned on." );
        }

        //
        // TODO: move checksum policies to 
        //
        if ( MavenExecutionRequest.CHECKSUM_POLICY_WARN.equals( cliRequest.request.getGlobalChecksumPolicy() ) )
        {
            logger.info( "Disabling strict checksum verification on all artifact downloads." );
        }
        else if ( MavenExecutionRequest.CHECKSUM_POLICY_FAIL.equals( cliRequest.request.getGlobalChecksumPolicy() ) )
        {
            logger.info( "Enabling strict checksum verification on all artifact downloads." );
        }
    }

    protected void showXavenInfo( final CliRequest cliRequest )
    {
        cliRequest.stdout.println( "-- Xaven Extensions Loaded --" );
        cliRequest.stdout.println();

        try
        {
            final Set<String> extensions = getLoadedExtensions();
            for ( final String ext : extensions )
            {
                cliRequest.stdout.println( "+" + ext );
            }
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to retrieve Xaven extension information: " + e.getMessage(), e );
        }

        cliRequest.stdout.println();
        cliRequest.stdout.println( "--------------------------" );
        cliRequest.stdout.println();
    }

    protected void properties( final CliRequest cliRequest )
    {
        populateProperties( cliRequest.commandLine, cliRequest.systemProperties, cliRequest.userProperties );
    }

    protected void container( final CliRequest cliRequest )
        throws Exception
    {
        if ( cliRequest.classWorld == null )
        {
            cliRequest.classWorld = new ClassWorld( "plexus.core", Thread.currentThread().getContextClassLoader() );
        }

        DefaultPlexusContainer container = this.container;

        if ( container == null )
        {
            final ContainerConfiguration cc =
                new XavenContainerConfiguration( cliRequest.xavenConfig ).setClassWorld( cliRequest.classWorld )
                                                                         .setName( "maven" );

            container = new DefaultPlexusContainer( cc );

            container.setLoggerManager( new MavenLoggerManager( logger ) );

            container.getLoggerManager().setThresholds( cliRequest.request.getLoggingLevel() );

            customizeContainer( container );

            if ( cliRequest.classWorld == classWorld )
            {
                this.container = container;
            }
        }

        maven = container.lookup( Maven.class );

        executionRequestPopulator = container.lookup( MavenExecutionRequestPopulator.class );

        modelProcessor = createModelProcessor( container );

        settingsBuilder = container.lookup( SettingsBuilder.class );

        dispatcher = (DefaultSecDispatcher) container.lookup( SecDispatcher.class, "maven" );
    }

    protected void customizeContainer( final PlexusContainer container )
    {
    }

    //
    // This should probably be a separate tool and not be baked into Maven.
    //
    protected void encryption( final CliRequest cliRequest )
        throws Exception
    {
        if ( cliRequest.commandLine.hasOption( CLIManager.ENCRYPT_MASTER_PASSWORD ) )
        {
            final String passwd = cliRequest.commandLine.getOptionValue( CLIManager.ENCRYPT_MASTER_PASSWORD );

            final DefaultPlexusCipher cipher = new DefaultPlexusCipher();

            cliRequest.stdout.println( cipher.encryptAndDecorate( passwd,
                                                                  DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION ) );

            throw new ExitException( 0 );
        }
        else if ( cliRequest.commandLine.hasOption( CLIManager.ENCRYPT_PASSWORD ) )
        {
            final String passwd = cliRequest.commandLine.getOptionValue( CLIManager.ENCRYPT_PASSWORD );

            String configurationFile = dispatcher.getConfigurationFile();

            if ( configurationFile.startsWith( "~" ) )
            {
                configurationFile = System.getProperty( "user.home" ) + configurationFile.substring( 1 );
            }

            final String file =
                System.getProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, configurationFile );

            String master = null;

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
            cliRequest.stdout.println( cipher.encryptAndDecorate( passwd, masterPasswd ) );

            throw new ExitException( 0 );
        }
    }

    protected int execute( final CliRequest cliRequest )
    {
        final MavenExecutionResult result = maven.execute( cliRequest.request );

        if ( result.hasExceptions() )
        {
            final ExceptionHandler handler = new DefaultExceptionHandler();

            final Map<String, String> references = new LinkedHashMap<String, String>();

            MavenProject project = null;

            for ( final Throwable exception : result.getExceptions() )
            {
                final ExceptionSummary summary = handler.handleException( exception );

                logSummary( summary, references, "", cliRequest.showErrors );

                if ( project == null && exception instanceof LifecycleExecutionException )
                {
                    project = ( (LifecycleExecutionException) exception ).getProject();
                }
            }

            logger.error( "" );

            if ( !cliRequest.showErrors )
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

            if ( MavenExecutionRequest.REACTOR_FAIL_NEVER.equals( cliRequest.request.getReactorFailureBehavior() ) )
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

    protected ModelProcessor createModelProcessor( final PlexusContainer container )
        throws ComponentLookupException
    {
        return container.lookup( ModelProcessor.class );
    }

    protected void settings( final CliRequest cliRequest )
        throws Exception
    {
        File userSettingsFile;

        if ( cliRequest.commandLine.hasOption( CLIManager.ALTERNATE_USER_SETTINGS ) )
        {
            userSettingsFile = new File( cliRequest.commandLine.getOptionValue( CLIManager.ALTERNATE_USER_SETTINGS ) );
            userSettingsFile = resolveFile( userSettingsFile, cliRequest.workingDirectory );

            if ( !userSettingsFile.isFile() )
            {
                throw new FileNotFoundException( "The specified user settings file does not exist: " + userSettingsFile );
            }
        }
        else
        {
            userSettingsFile = DEFAULT_USER_SETTINGS_FILE;
        }

        logger.debug( "Reading user settings from " + userSettingsFile );

        File globalSettingsFile;

        if ( cliRequest.commandLine.hasOption( CLIManager.ALTERNATE_GLOBAL_SETTINGS ) )
        {
            globalSettingsFile =
                new File( cliRequest.commandLine.getOptionValue( CLIManager.ALTERNATE_GLOBAL_SETTINGS ) );
            globalSettingsFile = resolveFile( globalSettingsFile, cliRequest.workingDirectory );

            if ( !globalSettingsFile.isFile() )
            {
                throw new FileNotFoundException( "The specified global settings file does not exist: "
                    + globalSettingsFile );
            }
        }
        else
        {
            globalSettingsFile = DEFAULT_GLOBAL_SETTINGS_FILE;
        }

        logger.debug( "Reading global settings from " + globalSettingsFile );

        cliRequest.request.setGlobalSettingsFile( globalSettingsFile );
        cliRequest.request.setUserSettingsFile( userSettingsFile );

        final SettingsBuildingRequest settingsRequest = new DefaultSettingsBuildingRequest();
        settingsRequest.setGlobalSettingsFile( globalSettingsFile );
        settingsRequest.setUserSettingsFile( userSettingsFile );
        settingsRequest.setSystemProperties( cliRequest.systemProperties );
        settingsRequest.setUserProperties( cliRequest.userProperties );

        final SettingsBuildingResult settingsResult = settingsBuilder.build( settingsRequest );

        executionRequestPopulator.populateFromSettings( cliRequest.request, settingsResult.getEffectiveSettings() );

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

    protected MavenExecutionRequest populateRequest( final CliRequest cliRequest )
    {
        final MavenExecutionRequest request = cliRequest.request;
        final CommandLine commandLine = cliRequest.commandLine;
        final String workingDirectory = cliRequest.workingDirectory;
        final boolean debug = cliRequest.debug;
        final boolean quiet = cliRequest.quiet;
        final boolean showErrors = cliRequest.showErrors;

        // ----------------------------------------------------------------------
        // Now that we have everything that we need we will fire up plexus and
        // bring the maven component to life for use.
        // ----------------------------------------------------------------------

        if ( commandLine.hasOption( CLIManager.BATCH_MODE ) )
        {
            request.setInteractiveMode( false );
        }

        boolean pluginUpdateOverride = false;

        if ( commandLine.hasOption( CLIManager.FORCE_PLUGIN_UPDATES )
            || commandLine.hasOption( CLIManager.FORCE_PLUGIN_UPDATES2 ) )
        {
            pluginUpdateOverride = true;
        }
        else if ( commandLine.hasOption( CLIManager.SUPPRESS_PLUGIN_UPDATES ) )
        {
            pluginUpdateOverride = false;
        }

        boolean noSnapshotUpdates = false;
        if ( commandLine.hasOption( CLIManager.SUPRESS_SNAPSHOT_UPDATES ) )
        {
            noSnapshotUpdates = true;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        @SuppressWarnings( "unchecked" )
        final List<String> goals = commandLine.getArgList();

        boolean recursive = true;

        // this is the default behavior.
        String reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_FAST;

        if ( commandLine.hasOption( CLIManager.NON_RECURSIVE ) )
        {
            recursive = false;
        }

        if ( commandLine.hasOption( CLIManager.FAIL_FAST ) )
        {
            reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_FAST;
        }
        else if ( commandLine.hasOption( CLIManager.FAIL_AT_END ) )
        {
            reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_AT_END;
        }
        else if ( commandLine.hasOption( CLIManager.FAIL_NEVER ) )
        {
            reactorFailureBehaviour = MavenExecutionRequest.REACTOR_FAIL_NEVER;
        }

        if ( commandLine.hasOption( CLIManager.OFFLINE ) )
        {
            request.setOffline( true );
        }

        boolean updateSnapshots = false;

        if ( commandLine.hasOption( CLIManager.UPDATE_SNAPSHOTS ) )
        {
            updateSnapshots = true;
        }

        String globalChecksumPolicy = null;

        if ( commandLine.hasOption( CLIManager.CHECKSUM_FAILURE_POLICY ) )
        {
            globalChecksumPolicy = MavenExecutionRequest.CHECKSUM_POLICY_FAIL;
        }
        else if ( commandLine.hasOption( CLIManager.CHECKSUM_WARNING_POLICY ) )
        {
            globalChecksumPolicy = MavenExecutionRequest.CHECKSUM_POLICY_WARN;
        }

        final File baseDirectory = new File( workingDirectory, "" ).getAbsoluteFile();

        // ----------------------------------------------------------------------
        // Profile Activation
        // ----------------------------------------------------------------------

        final List<String> activeProfiles = new ArrayList<String>();

        final List<String> inactiveProfiles = new ArrayList<String>();

        if ( commandLine.hasOption( CLIManager.ACTIVATE_PROFILES ) )
        {
            final String[] profileOptionValues = commandLine.getOptionValues( CLIManager.ACTIVATE_PROFILES );
            if ( profileOptionValues != null )
            {
                for ( int i = 0; i < profileOptionValues.length; ++i )
                {
                    final StringTokenizer profileTokens = new StringTokenizer( profileOptionValues[i], "," );

                    while ( profileTokens.hasMoreTokens() )
                    {
                        final String profileAction = profileTokens.nextToken().trim();

                        if ( profileAction.startsWith( "-" ) || profileAction.startsWith( "!" ) )
                        {
                            inactiveProfiles.add( profileAction.substring( 1 ) );
                        }
                        else if ( profileAction.startsWith( "+" ) )
                        {
                            activeProfiles.add( profileAction.substring( 1 ) );
                        }
                        else
                        {
                            activeProfiles.add( profileAction );
                        }
                    }
                }
            }
        }

        ArtifactTransferListener transferListener;

        if ( request.isInteractiveMode() )
        {
            transferListener = new InteractiveTransferListener( cliRequest.stdout );
        }
        else
        {
            transferListener = new BatchTransferListener( cliRequest.stdout );
        }

        transferListener.setShowChecksumEvents( false );

        String alternatePomFile = null;
        if ( commandLine.hasOption( CLIManager.ALTERNATE_POM_FILE ) )
        {
            alternatePomFile = commandLine.getOptionValue( CLIManager.ALTERNATE_POM_FILE );
        }

        int loggingLevel;

        if ( debug )
        {
            loggingLevel = MavenExecutionRequest.LOGGING_LEVEL_DEBUG;
        }
        else if ( quiet )
        {
            // TODO: we need to do some more work here. Some plugins use sys out or log errors at info level.
            // Ideally, we could use Warn across the board
            loggingLevel = MavenExecutionRequest.LOGGING_LEVEL_ERROR;
            // TODO:Additionally, we can't change the mojo level because the component key includes the version and it isn't known ahead of time. This seems worth changing.
        }
        else
        {
            loggingLevel = MavenExecutionRequest.LOGGING_LEVEL_INFO;
        }

        File userToolchainsFile;
        if ( commandLine.hasOption( CLIManager.ALTERNATE_USER_TOOLCHAINS ) )
        {
            userToolchainsFile = new File( commandLine.getOptionValue( CLIManager.ALTERNATE_USER_TOOLCHAINS ) );
            userToolchainsFile = resolveFile( userToolchainsFile, workingDirectory );
        }
        else
        {
            userToolchainsFile = XavenMain.DEFAULT_USER_TOOLCHAINS_FILE;
        }

        request.setBaseDirectory( baseDirectory )
               .setGoals( goals )
               .setSystemProperties( cliRequest.systemProperties )
               .setUserProperties( cliRequest.userProperties )
               .setReactorFailureBehavior( reactorFailureBehaviour )
               // default: fail fast
               .setRecursive( recursive )
               // default: true
               .setShowErrors( showErrors )
               // default: false
               .setUsePluginUpdateOverride( pluginUpdateOverride )
               .addActiveProfiles( activeProfiles )
               // optional
               .addInactiveProfiles( inactiveProfiles )
               // optional
               .setLoggingLevel( loggingLevel )
               // default: info
               .setTransferListener( transferListener )
               // default: batch mode which goes along with interactive
               .setUpdateSnapshots( updateSnapshots )
               // default: false
               .setNoSnapshotUpdates( noSnapshotUpdates )
               // default: false
               .setGlobalChecksumPolicy( globalChecksumPolicy )
               // default: warn
               .setUserToolchainsFile( userToolchainsFile );

        if ( alternatePomFile != null )
        {
            final File pom = resolveFile( new File( alternatePomFile ), workingDirectory );

            request.setPom( pom );
        }
        else
        {
            final File pom = modelProcessor.locatePom( baseDirectory );

            if ( pom.isFile() )
            {
                request.setPom( pom );
            }
        }

        if ( ( request.getPom() != null ) && ( request.getPom().getParentFile() != null ) )
        {
            request.setBaseDirectory( request.getPom().getParentFile() );
        }

        if ( commandLine.hasOption( CLIManager.RESUME_FROM ) )
        {
            request.setResumeFrom( commandLine.getOptionValue( CLIManager.RESUME_FROM ) );
        }

        if ( commandLine.hasOption( CLIManager.PROJECT_LIST ) )
        {
            final String projectList = commandLine.getOptionValue( CLIManager.PROJECT_LIST );
            final String[] projects = StringUtils.split( projectList, "," );
            request.setSelectedProjects( Arrays.asList( projects ) );
        }

        if ( commandLine.hasOption( CLIManager.ALSO_MAKE ) && !commandLine.hasOption( CLIManager.ALSO_MAKE_DEPENDENTS ) )
        {
            request.setMakeBehavior( MavenExecutionRequest.REACTOR_MAKE_UPSTREAM );
        }
        else if ( !commandLine.hasOption( CLIManager.ALSO_MAKE )
            && commandLine.hasOption( CLIManager.ALSO_MAKE_DEPENDENTS ) )
        {
            request.setMakeBehavior( MavenExecutionRequest.REACTOR_MAKE_DOWNSTREAM );
        }
        else if ( commandLine.hasOption( CLIManager.ALSO_MAKE )
            && commandLine.hasOption( CLIManager.ALSO_MAKE_DEPENDENTS ) )
        {
            request.setMakeBehavior( MavenExecutionRequest.REACTOR_MAKE_BOTH );
        }

        String localRepoProperty = request.getUserProperties().getProperty( XavenMain.LOCAL_REPO_PROPERTY );

        if ( localRepoProperty == null )
        {
            localRepoProperty = request.getSystemProperties().getProperty( XavenMain.LOCAL_REPO_PROPERTY );
        }

        if ( localRepoProperty != null )
        {
            request.setLocalRepositoryPath( localRepoProperty );
        }

        final String threadConfiguration =
            commandLine.hasOption( CLIManager.THREADS ) ? commandLine.getOptionValue( CLIManager.THREADS )
                            : request.getSystemProperties().getProperty( XavenMain.THREADS_DEPRECATED ); // TODO: Remove this setting. Note that the int-tests use it

        if ( threadConfiguration != null )
        {
            request.setPerCoreThreadCount( threadConfiguration.contains( "C" ) );
            if ( threadConfiguration.contains( "W" ) )
            {
                LifecycleWeaveBuilder.setWeaveMode( request.getUserProperties() );
            }
            request.setThreadCount( threadConfiguration.replace( "C", "" ).replace( "W", "" ).replace( "auto", "" ) );
        }

        return request;
    }

    protected File resolveFile( final File file, final String workingDirectory )
    {
        if ( file == null )
        {
            return null;
        }
        else if ( file.isAbsolute() )
        {
            return file;
        }
        else if ( file.getPath().startsWith( File.separator ) )
        {
            // drive-relative Windows path
            return file.getAbsoluteFile();
        }
        else
        {
            return new File( workingDirectory, file.getPath() ).getAbsoluteFile();
        }
    }

    // ----------------------------------------------------------------------
    // System properties handling
    // ----------------------------------------------------------------------

    protected void populateProperties( final CommandLine commandLine, final Properties systemProperties,
                                       final Properties userProperties )
    {
        EnvironmentUtils.addEnvVars( systemProperties );

        // ----------------------------------------------------------------------
        // Options that are set on the command line become system properties
        // and therefore are set in the session properties. System properties
        // are most dominant.
        // ----------------------------------------------------------------------

        if ( commandLine.hasOption( CLIManager.SET_SYSTEM_PROPERTY ) )
        {
            final String[] defStrs = commandLine.getOptionValues( CLIManager.SET_SYSTEM_PROPERTY );

            if ( defStrs != null )
            {
                for ( int i = 0; i < defStrs.length; ++i )
                {
                    setCliProperty( defStrs[i], userProperties );
                }
            }
        }

        systemProperties.putAll( System.getProperties() );
    }

    protected static void setCliProperty( final String property, final Properties properties )
    {
        String name;

        String value;

        final int i = property.indexOf( "=" );

        if ( i <= 0 )
        {
            name = property.trim();

            value = "true";
        }
        else
        {
            name = property.substring( 0, i ).trim();

            value = property.substring( i + 1 );
        }

        properties.setProperty( name, value );

        // ----------------------------------------------------------------------
        // I'm leaving the setting of system properties here as not to break
        // the SystemPropertyProfileActivator. This won't harm embedding. jvz.
        // ----------------------------------------------------------------------

        System.setProperty( name, value );
    }

    protected void xavenConfiguration( final CliRequest cliRequest )
    {
        final XavenConfiguration config =
            new XavenConfiguration().withMavenExecutionRequest( cliRequest.request )
                                    .withConfigurationDirectory( CONFIGURATION_DIRECTORY );

        if ( cliRequest.debug )
        {
            config.withDebug();
        }
        else
        {
            config.withoutDebug();
        }

        if ( cliRequest.commandLine.hasOption( CLIManager.BATCH_MODE ) )
        {
            config.nonInteractive();
        }
        else
        {
            config.interactive();
        }

        try
        {
            config.withComponentSelections( getComponentOverrides() );
            config.withExtensionConfigurations( loadExtensionConfigurations( config ) );
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to query context classloader for component-overrides files. Reason: "
                + e.getMessage(), e );
        }

        cliRequest.xavenConfig = config;
    }

    public static class ExitException
        extends Exception
    {

        private static final long serialVersionUID = 1L;

        public int exitCode;

        public ExitException( final int exitCode )
        {
            this.exitCode = exitCode;
        }

    }

}
