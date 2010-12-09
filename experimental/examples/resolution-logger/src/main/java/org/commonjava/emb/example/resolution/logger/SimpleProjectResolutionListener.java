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

package org.commonjava.emb.example.resolution.logger;

import static org.codehaus.plexus.util.IOUtil.close;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.event.EMBAsyncEventListener;
import org.commonjava.emb.event.EMBEvent;
import org.commonjava.emb.event.resolver.ProjectDependencyResolutionEvent;
import org.commonjava.emb.event.resolver.ResolutionEvent;
import org.commonjava.emb.event.resolver.ResolutionEventType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

@Component( role = EMBAsyncEventListener.class, hint = "simple-project-resolution-listener" )
public class SimpleProjectResolutionListener
    implements EMBAsyncEventListener, Initializable
{

    @Requirement
    private EMBConfiguration embConfiguration;

    @Requirement( hint = "example-resolution-logger" )
    private EMBLibrary library;

    private File dataDir;

    public boolean canHandle( final EMBEvent event )
    {
        return ( event instanceof ResolutionEvent )
            && ( (ResolutionEvent) event ).getType() == ResolutionEventType.SUCCESS;
    }

    public void handle( final EMBEvent evt )
    {
        if ( evt instanceof ProjectDependencyResolutionEvent )
        {
            final ProjectDependencyResolutionEvent event = (ProjectDependencyResolutionEvent) evt;

            final Collection<? extends MavenProject> projects = event.getProjects();
            final Set<org.apache.maven.artifact.Artifact> resolvedArtifacts = event.getResolvedArtifacts();

            final StringBuilder sb = new StringBuilder();
            sb.append( "Projects Resolved:\n---------------------\n\n" );
            for ( final MavenProject mavenProject : projects )
            {
                sb.append( "  " ).append( mavenProject.getId() ).append( "\n" );
            }

            appendArtifactInfo( resolvedArtifacts, sb );

            for ( final MavenProject project : projects )
            {
                writeFile( sb,
                           new File( project.getBuild().getDirectory(), "project-" + project.getId().replace( ':', '_' )
                               + ".resolver.log" ) );
            }
        }
    }

    public void initialize()
        throws InitializationException
    {
        final File confDir = embConfiguration.getConfigurationDirectory();
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

    private void appendArtifactInfo( final Set<Artifact> resolvedArtifacts, final StringBuilder sb )
    {
        sb.append( "Artifacts Resolved:\n---------------------\n\n" );

        if ( resolvedArtifacts != null && !resolvedArtifacts.isEmpty() )
        {
            for ( final Artifact artifact : resolvedArtifacts )
            {
                sb.append( "  " )
                  .append( artifact.getId() )
                  .append( "\n    File: " )
                  .append( artifact.getFile() )
                  .append( "\n    Repository: " );
                if ( artifact.getRepository() == null )
                {
                    sb.append( "-UNKNOWN-" );
                }
                else
                {
                    sb.append( artifact.getRepository().getId() )
                      .append( ": " )
                      .append( artifact.getRepository().getUrl() );
                }

                sb.append( "\n\n" );
            }
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
