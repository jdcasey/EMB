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

package org.commonjava.xaven.example.resolution.logger;

import static org.codehaus.plexus.util.IOUtil.close;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.event.XavenAsyncEventListener;
import org.commonjava.xaven.event.XavenEvent;
import org.commonjava.xaven.event.resolver.PluginResolutionEvent;
import org.commonjava.xaven.event.resolver.ProjectDependencyResolutionEvent;
import org.commonjava.xaven.event.resolver.ResolutionEvent;
import org.commonjava.xaven.event.resolver.ResolutionEventType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component( role = XavenAsyncEventListener.class, hint = "simple-resolution-listener" )
public class SimpleResolutionListener
    implements XavenAsyncEventListener, Initializable
{

    @Requirement
    private XavenConfiguration xavenConfiguration;

    @Requirement( hint = "sample-resolution-logger" )
    private XavenLibrary library;

    private File dataDir;

    public boolean canHandle( final XavenEvent event )
    {
        return ( event instanceof ResolutionEvent )
            && ( (ResolutionEvent) event ).getType() == ResolutionEventType.SUCCESS;
    }

    public void handle( final XavenEvent event )
    {
        if ( event instanceof ProjectDependencyResolutionEvent )
        {
            logProjectResolution( (ProjectDependencyResolutionEvent) event );
        }
        else if ( event instanceof PluginResolutionEvent )
        {
            logPluginResolution( (PluginResolutionEvent) event );
        }
    }

    private void logPluginResolution( final PluginResolutionEvent event )
    {
        final Plugin plugin = event.getPlugin();
        final List<Artifact> resolvedArtifacts = event.getResolvedArtifacts();

        final StringBuilder sb = new StringBuilder();
        sb.append( "Plugin: " ).append( plugin.getId() ).append( "\n\n" );

        appendArtifactInfo( resolvedArtifacts, sb );

        writeFile( sb, new File( dataDir, "plugin-" + plugin.getId().replace( ':', '_' ) + ".resolver.log" ) );
    }

    private void logProjectResolution( final ProjectDependencyResolutionEvent event )
    {
        final Collection<? extends MavenProject> projects = event.getProjects();
        final Set<Artifact> resolvedArtifacts = event.getResolvedArtifacts();

        final StringBuilder sb = new StringBuilder();
        sb.append( "Projects Resolved:\n---------------------\n\n" );
        for ( final MavenProject mavenProject : projects )
        {
            sb.append( "  " ).append( mavenProject.getId() ).append( "\n" );
        }

        appendArtifactInfo( resolvedArtifacts, sb );

        for ( final MavenProject project : projects )
        {
            writeFile( sb, new File( project.getBuild().getDirectory(), "project-" + project.getId().replace( ':', '_' )
                + ".resolver.log" ) );
        }
    }

    public void initialize()
        throws InitializationException
    {
        final File confDir = xavenConfiguration.getConfigurationDirectory();
        dataDir = new File( confDir, "data" );
        try
        {
            dataDir = dataDir.getCanonicalFile();
        }
        catch ( final IOException e )
        {
            dataDir = dataDir.getAbsoluteFile();
        }

        dataDir.mkdirs();
    }

    private void appendArtifactInfo( final Collection<Artifact> resolvedArtifacts, final StringBuilder sb )
    {
        sb.append( "Artifacts Resolved:\n---------------------\n\n" );
        for ( final Artifact artifact : resolvedArtifacts )
        {
            if ( artifact == null )
            {
                continue;
            }

            sb.append( "  " )
              .append( artifact.getId() )
              .append( "\n    File: " )
              .append( artifact.getFile() )
              .append( "\n    Repository: " )
              .append( artifact.getRepository() == null ? "-UNKNOWN-" : artifact.getRepository().getUrl() )
              .append( "\n\n" );
        }
    }

    private void writeFile( final StringBuilder sb, final File file )
    {
        if ( library.getLogger().isDebugEnabled() )
        {
            library.getLogger().debug( "Writing: " + file.getAbsolutePath() );
        }

        FileWriter writer = null;
        try
        {
            writer = new FileWriter( file );
            writer.write( sb.toString() );
        }
        catch ( final IOException e )
        {
            library.getLogger()
                   .error( "Failed to write : " + file.getAbsolutePath() + "\nReason: " + e.getMessage(), e );
        }
        finally
        {
            close( writer );
        }
    }

}
