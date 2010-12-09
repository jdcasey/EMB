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

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.event.EMBAsyncEventListener;
import org.commonjava.emb.event.EMBEvent;
import org.commonjava.emb.event.resolver.PluginResolutionEvent;
import org.commonjava.emb.event.resolver.ResolutionEvent;
import org.commonjava.emb.event.resolver.ResolutionEventType;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.Dependency;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RemoteRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

@Component( role = EMBAsyncEventListener.class, hint = "simple-plugin-resolution-listener" )
public class SimplePluginResolutionListener
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
        if ( evt instanceof PluginResolutionEvent )
        {
            final PluginResolutionEvent event = (PluginResolutionEvent) evt;
            final Plugin plugin = event.getPlugin();
            final DependencyNode node = event.getResolvedNode();

            final StringBuilder sb = new StringBuilder();
            sb.append( "Plugin: " ).append( plugin.getId() ).append( "\n\n" );

            appendArtifactInfo( node, sb );

            writeFile( sb, new File( dataDir, "plugin-" + plugin.getId().replace( ':', '_' ) + ".resolver.log" ) );
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

    private void appendArtifactInfo( final DependencyNode node, final StringBuilder sb )
    {
        sb.append( "Artifacts Resolved:\n---------------------\n\n" );

        final LinkedList<DependencyNode> nodes = new LinkedList<DependencyNode>();
        nodes.add( node );

        while ( !nodes.isEmpty() )
        {
            final DependencyNode dn = nodes.removeFirst();
            final Dependency d = dn.getDependency();
            final Artifact a = d == null ? null : d.getArtifact();

            sb.append( "  " )
              .append( dn.getConflictId() )
              .append( "\n    File: " )
              .append( a == null ? "-UNKNOWN-" : a.getFile() )
              .append( "\n    Repositories:\n" );

            if ( dn.getRepositories() != null )
            {
                for ( final RemoteRepository repo : dn.getRepositories() )
                {
                    sb.append( "\n\t" ).append( repo.getId() ).append( ": " ).append( repo.getUrl() );
                }
            }
            else
            {
                sb.append( "\n\t-UNKNOWN-" );
            }

            sb.append( "\n\n" );

            if ( dn.getChildren() != null && !dn.getChildren().isEmpty() )
            {
                nodes.addAll( dn.getChildren() );
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
