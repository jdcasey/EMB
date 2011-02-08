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

package org.commonjava.emb.project.graph;

import org.apache.log4j.Logger;
import org.apache.maven.artifact.ArtifactUtils;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DepGraphNode
    implements Iterable<Throwable>
{

    private static final Logger LOGGER = Logger.getLogger( DepGraphNode.class );

    private final Set<DisconnectedDepNode> nodes = new LinkedHashSet<DisconnectedDepNode>();

    private Artifact latestArtifact;

    private ArtifactResult latestResult;

    private final LinkedHashSet<RemoteRepository> remoteRepositories = new LinkedHashSet<RemoteRepository>();

    private final Map<String, ArtifactResult> results = new HashMap<String, ArtifactResult>();

    private String key;

    private final boolean preResolved;

    public DepGraphNode( final DependencyNode node )
    {
        this( node, null, false );
    }

    protected DepGraphNode( final DependencyNode node, final String key, final boolean preResolved )
    {
        merge( node );

        if ( key == null )
        {
            if ( latestArtifact != null )
            {
                this.key = key( latestArtifact );
            }
            else
            {
                throw new NullPointerException(
                                                "Cannot calculate node key. DependencyNode parameter does not contain a valid artifact!" );
            }
        }
        else
        {
            this.key = key;
        }

        this.preResolved = preResolved;
    }

    public DepGraphNode( final Artifact artifact, final boolean preResolved )
    {
        key = key( artifact );
        latestArtifact = artifact;
        this.preResolved = preResolved;
    }

    static String key( final Artifact a )
    {
        return ArtifactUtils.key( a.getGroupId(), a.getArtifactId(), a.getBaseVersion() );
    }

    public boolean isPreResolved()
    {
        return preResolved;
    }

    public void merge( final DependencyNode node )
    {
        nodes.add( new DisconnectedDepNode( node ) );
        if ( node.getRepositories() != null )
        {
            remoteRepositories.addAll( node.getRepositories() );
        }

        if ( latestArtifact == null && node.getDependency() != null && node.getDependency().getArtifact() != null )
        {
            latestArtifact = node.getDependency().getArtifact();
        }
    }

    public synchronized void merge( final ArtifactResult result )
    {
        if ( result.getArtifact() != null && result.getArtifact().getFile() != null )
        {
            result.getExceptions().clear();
        }

        String ext = null;
        if ( result.getArtifact() != null )
        {
            ext = result.getArtifact().getExtension();
            latestArtifact = result.getArtifact();
        }
        else if ( result.getRequest() != null && result.getRequest().getArtifact() != null )
        {
            ext = result.getRequest().getArtifact().getExtension();
            latestArtifact = result.getRequest().getArtifact();
        }
        else if ( latestArtifact != null )
        {
            ext = latestArtifact.getExtension();
        }
        else if ( !results.containsKey( "pom" ) )
        {
            ext = "pom";
        }
        else
        {
            if ( LOGGER.isDebugEnabled() )
            {
                LOGGER.debug( "PANIC: Cannot find artifact extension to file artifact result (project: " + getKey()
                                + ": " + result );
            }
        }

        if ( ext != null )
        {
            final ArtifactResult existing = results.get( ext );
            if ( existing != null && existing.getArtifact() != null )
            {
                if ( LOGGER.isDebugEnabled() )
                {
                    LOGGER.debug( "PANIC: Result-map already contains result with resolved artifact for: " + ext );
                }
            }
            else
            {
                results.put( ext, result );
                latestResult = result;
            }
        }
    }

    public ArtifactResult getResult( final String extension )
    {
        return results.get( extension );
    }

    public Collection<ArtifactResult> getResults()
    {
        return new HashSet<ArtifactResult>( results.values() );
    }

    public synchronized ArtifactResult getLatestResult()
    {
        return latestResult;
    }

    public synchronized Artifact getLatestArtifact()
    {
        return latestArtifact;
    }

    public LinkedHashSet<RemoteRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }

    public String getKey()
    {
        return key;
    }

    public synchronized boolean hasErrors()
    {
        for ( final ArtifactResult result : results.values() )
        {
            if ( result.getExceptions() != null && !result.getExceptions().isEmpty() )
            {
                return true;
            }
        }

        return false;
    }

    private String renderErrors()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append( "Failed to resolve: " ).append( getKey() );
        // sb.append( "\nDependency of:" );
        //
        // for ( final List<String> parentTrail : getDependencyTrails() )
        // {
        // int indents = 0;
        // for ( final String node : parentTrail )
        // {
        // sb.append( "\n" ).append( indents ).append( ": " );
        // for ( int i = 0; i < indents; i++ )
        // {
        // sb.append( "  " );
        // }
        // sb.append( node );
        // indents++;
        //
        // if ( indents > 6 )
        // {
        // sb.append( "..." );
        // break;
        // }
        // }
        //
        // sb.append( "\n" );
        // }

        sb.append( "\n\n" )
          .append( results.size() )
          .append( " Resolution attempts (may be for different artifacts within the same project):\n" );

        for ( final Map.Entry<String, ArtifactResult> entry : results.entrySet() )
        {
            final ArtifactResult result = entry.getValue();
            if ( result == null )
            {
                continue;
            }

            final List<Exception> errors = result.getExceptions();
            if ( errors != null && !errors.isEmpty() )
            {
                sb.append( "Errors for artifact of type: '" ).append( entry.getKey() ).append( "':" );

                for ( final Exception error : errors )
                {
                    final StringWriter sWriter = new StringWriter();
                    error.printStackTrace( new PrintWriter( sWriter ) );

                    sb.append( "\n\n" ).append( sWriter.toString() );
                }
            }
            else
            {
                sb.append( "No resolution errors recorded for artifact of type: '" )
                  .append( entry.getKey() )
                  .append( "'." );
            }
        }

        return sb.toString();
    }

    @Override
    public Iterator<Throwable> iterator()
    {
        return getErrors().iterator();
    }

    public List<Throwable> getErrors()
    {
        final List<Throwable> errors = new ArrayList<Throwable>();
        for ( final ArtifactResult result : results.values() )
        {
            if ( result.getExceptions() != null && !result.getExceptions().isEmpty() )
            {
                errors.addAll( result.getExceptions() );
            }
        }

        return errors;
    }

    public void logErrors( final PrintWriter writer )
    {
        writer.println( renderErrors() );
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append( "DependencyTrackingState (\n    latestArtifact=" );
        builder.append( latestArtifact );
        builder.append( "\n    latestResult=" );
        builder.append( latestResult );
        builder.append( "\n    projectId=" );
        builder.append( key );
        builder.append( "\n    results=" );
        builder.append( results );
        builder.append( "\n)" );
        return builder.toString();
    }

    public void removeResult( final String ext )
    {
        results.remove( ext );
    }

    public void merge( final Artifact child )
    {
        merge( new ArtifactOnlyDependencyNode( child ) );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final DepGraphNode other = (DepGraphNode) obj;
        if ( key == null )
        {
            if ( other.key != null )
            {
                return false;
            }
        }
        else if ( !key.equals( other.key ) )
        {
            return false;
        }
        return true;
    }
}
