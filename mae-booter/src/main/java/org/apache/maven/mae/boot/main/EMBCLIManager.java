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

package org.apache.maven.mae.boot.main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.maven.cli.CLIManager;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class EMBCLIManager
{

    public static final String XAVEN_DEBUG_LOG_HANDLES = "ZX";

    private final Options options;

    @SuppressWarnings( "static-access" )
    public EMBCLIManager()
    {
        options = new Options();

        options.addOption( OptionBuilder.withLongOpt( "debug-emb" )
                                        .hasArg()
                                        .withDescription( "Comma-separated list of EMB log-handles to debug." )
                                        .create( XAVEN_DEBUG_LOG_HANDLES ) );

        populateNativeMavenOptions( options );
    }

    @SuppressWarnings( "static-access" )
    private void populateNativeMavenOptions( final Options options )
    {
        options.addOption( OptionBuilder.withLongOpt( "file" )
                                        .hasArg()
                                        .withDescription( "Force the use of an alternate POM file." )
                                        .create( CLIManager.ALTERNATE_POM_FILE ) );

        options.addOption( OptionBuilder.withLongOpt( "define" )
                                        .hasArg()
                                        .withDescription( "Define a system property" )
                                        .create( CLIManager.SET_SYSTEM_PROPERTY ) );

        options.addOption( OptionBuilder.withLongOpt( "offline" )
                                        .withDescription( "Work offline" )
                                        .create( CLIManager.OFFLINE ) );

        options.addOption( OptionBuilder.withLongOpt( "help" )
                                        .withDescription( "Display help information" )
                                        .create( CLIManager.HELP ) );

        options.addOption( OptionBuilder.withLongOpt( "version" )
                                        .withDescription( "Display version information" )
                                        .create( CLIManager.VERSION ) );

        options.addOption( OptionBuilder.withLongOpt( "quiet" )
                                        .withDescription( "Quiet output - only show errors" )
                                        .create( CLIManager.QUIET ) );

        options.addOption( OptionBuilder.withLongOpt( "debug" )
                                        .withDescription( "Produce execution debug output" )
                                        .create( CLIManager.DEBUG ) );

        options.addOption( OptionBuilder.withLongOpt( "errors" )
                                        .withDescription( "Produce execution error messages" )
                                        .create( CLIManager.ERRORS ) );

        options.addOption( OptionBuilder.withLongOpt( "non-recursive" )
                                        .withDescription( "Do not recurse into sub-projects" )
                                        .create( CLIManager.NON_RECURSIVE ) );

        options.addOption( OptionBuilder.withLongOpt( "update-snapshots" )
                                        .withDescription( "Forces a check for updated releases and snapshots on remote repositories" )
                                        .create( CLIManager.UPDATE_SNAPSHOTS ) );

        options.addOption( OptionBuilder.withLongOpt( "activate-profiles" )
                                        .withDescription( "Comma-delimited list of profiles to activate" )
                                        .hasArg()
                                        .create( CLIManager.ACTIVATE_PROFILES ) );

        options.addOption( OptionBuilder.withLongOpt( "batch-mode" )
                                        .withDescription( "Run in non-interactive (batch) mode" )
                                        .create( CLIManager.BATCH_MODE ) );

        options.addOption( OptionBuilder.withLongOpt( "no-snapshot-updates" )
                                        .withDescription( "Supress SNAPSHOT updates" )
                                        .create( CLIManager.SUPRESS_SNAPSHOT_UPDATES ) );

        options.addOption( OptionBuilder.withLongOpt( "strict-checksums" )
                                        .withDescription( "Fail the build if checksums don't match" )
                                        .create( CLIManager.CHECKSUM_FAILURE_POLICY ) );

        options.addOption( OptionBuilder.withLongOpt( "lax-checksums" )
                                        .withDescription( "Warn if checksums don't match" )
                                        .create( CLIManager.CHECKSUM_WARNING_POLICY ) );

        options.addOption( OptionBuilder.withLongOpt( "settings" )
                                        .withDescription( "Alternate path for the user settings file" )
                                        .hasArg()
                                        .create( CLIManager.ALTERNATE_USER_SETTINGS ) );

        options.addOption( OptionBuilder.withLongOpt( "global-settings" )
                                        .withDescription( "Alternate path for the global settings file" )
                                        .hasArg()
                                        .create( CLIManager.ALTERNATE_GLOBAL_SETTINGS ) );

        options.addOption( OptionBuilder.withLongOpt( "toolchains" )
                                        .withDescription( "Alternate path for the user toolchains file" )
                                        .hasArg()
                                        .create( CLIManager.ALTERNATE_USER_TOOLCHAINS ) );

        options.addOption( OptionBuilder.withLongOpt( "fail-fast" )
                                        .withDescription( "Stop at first failure in reactorized builds" )
                                        .create( CLIManager.FAIL_FAST ) );

        options.addOption( OptionBuilder.withLongOpt( "fail-at-end" )
                                        .withDescription( "Only fail the build afterwards; allow all non-impacted builds to continue" )
                                        .create( CLIManager.FAIL_AT_END ) );

        options.addOption( OptionBuilder.withLongOpt( "fail-never" )
                                        .withDescription( "NEVER fail the build, regardless of project result" )
                                        .create( CLIManager.FAIL_NEVER ) );

        options.addOption( OptionBuilder.withLongOpt( "resume-from" )
                                        .hasArg()
                                        .withDescription( "Resume reactor from specified project" )
                                        .create( CLIManager.RESUME_FROM ) );

        options.addOption( OptionBuilder.withLongOpt( "projects" )
                                        .withDescription( "Build specified reactor projects instead of all projects. A project can be specified by [groupId]:artifactId or by its relative path." )
                                        .hasArg()
                                        .create( CLIManager.PROJECT_LIST ) );

        options.addOption( OptionBuilder.withLongOpt( "also-make" )
                                        .withDescription( "If project list is specified, also build projects required by the list" )
                                        .create( CLIManager.ALSO_MAKE ) );

        options.addOption( OptionBuilder.withLongOpt( "also-make-dependents" )
                                        .withDescription( "If project list is specified, also build projects that depend on projects on the list" )
                                        .create( CLIManager.ALSO_MAKE_DEPENDENTS ) );

        options.addOption( OptionBuilder.withLongOpt( "log-file" )
                                        .hasArg()
                                        .withDescription( "Log file to where all build output will go." )
                                        .create( CLIManager.LOG_FILE ) );

        options.addOption( OptionBuilder.withLongOpt( "show-version" )
                                        .withDescription( "Display version information WITHOUT stopping build" )
                                        .create( CLIManager.SHOW_VERSION ) );

        options.addOption( OptionBuilder.withLongOpt( "encrypt-master-password" )
                                        .hasArg()
                                        .withDescription( "Encrypt master security password" )
                                        .create( CLIManager.ENCRYPT_MASTER_PASSWORD ) );

        options.addOption( OptionBuilder.withLongOpt( "encrypt-password" )
                                        .hasArg()
                                        .withDescription( "Encrypt server password" )
                                        .create( CLIManager.ENCRYPT_PASSWORD ) );

        options.addOption( OptionBuilder.withLongOpt( "threads" )
                                        .hasArg()
                                        .withDescription( "Thread count, for instance 2.0C where C is core multiplied" )
                                        .create( CLIManager.THREADS ) );

        // Adding this back in for compatibility with the verifier that hard codes this option.

        options.addOption( OptionBuilder.withLongOpt( "no-plugin-registry" )
                                        .withDescription( "Ineffective, only kept for backward compatibility" )
                                        .create( "npr" ) );
    }

    public CommandLine parse( final String[] args )
        throws ParseException
    {
        // We need to eat any quotes surrounding arguments...
        final String[] cleanArgs = cleanArgs( args );

        final CommandLineParser parser = new GnuParser();

        return parser.parse( options, cleanArgs );
    }

    private String[] cleanArgs( final String[] args )
    {
        final List<String> cleaned = new ArrayList<String>();

        StringBuilder currentArg = null;

        for ( int i = 0; i < args.length; i++ )
        {
            final String arg = args[i];

            boolean addedToBuffer = false;

            if ( arg.startsWith( "\"" ) )
            {
                // if we're in the process of building up another arg, push it and start over.
                // this is for the case: "-Dfoo=bar "-Dfoo2=bar two" (note the first unterminated quote)
                if ( currentArg != null )
                {
                    cleaned.add( currentArg.toString() );
                }

                // start building an argument here.
                currentArg = new StringBuilder( arg.substring( 1 ) );
                addedToBuffer = true;
            }

            // this has to be a separate "if" statement, to capture the case of: "-Dfoo=bar"
            if ( arg.endsWith( "\"" ) )
            {
                final String cleanArgPart = arg.substring( 0, arg.length() - 1 );

                // if we're building an argument, keep doing so.
                if ( currentArg != null )
                {
                    // if this is the case of "-Dfoo=bar", then we need to adjust the buffer.
                    if ( addedToBuffer )
                    {
                        currentArg.setLength( currentArg.length() - 1 );
                    }
                    // otherwise, we trim the trailing " and append to the buffer.
                    else
                    {
                        // TODO: introducing a space here...not sure what else to do but collapse whitespace
                        currentArg.append( ' ' ).append( cleanArgPart );
                    }

                    cleaned.add( currentArg.toString() );
                }
                else
                {
                    cleaned.add( cleanArgPart );
                }

                currentArg = null;

                continue;
            }

            // if we haven't added this arg to the buffer, and we ARE building an argument
            // buffer, then append it with a preceding space...again, not sure what else to
            // do other than collapse whitespace.
            // NOTE: The case of a trailing quote is handled by nullifying the arg buffer.
            if ( !addedToBuffer )
            {
                if ( currentArg != null )
                {
                    currentArg.append( ' ' ).append( arg );
                }
                else
                {
                    cleaned.add( arg );
                }
            }
        }

        if ( currentArg != null )
        {
            cleaned.add( currentArg.toString() );
        }

        final int cleanedSz = cleaned.size();

        String[] cleanArgs = null;

        if ( cleanedSz == 0 )
        {
            cleanArgs = args;
        }
        else
        {
            cleanArgs = cleaned.toArray( new String[cleanedSz] );
        }

        return cleanArgs;
    }

    public void displayHelp( final PrintStream stdout )
    {
        stdout.println();

        final PrintWriter pw = new PrintWriter( stdout );

        final HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp( pw, HelpFormatter.DEFAULT_WIDTH, "xvn [options] [<goal(s)>] [<phase(s)>]", "\nOptions:",
                             options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "\n", false );

        pw.flush();
    }

}
