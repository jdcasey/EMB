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

package org.commonjava.emb.project;

import static org.apache.maven.artifact.ArtifactUtils.key;

import org.apache.log4j.Logger;
import org.apache.maven.project.MavenProject;
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

public class DependencyTracker
    implements Iterable<Throwable>
{

    private static final Logger LOGGER = Logger.getLogger( DependencyTracker.class );

    private final Set<DependencyNode> nodes = new LinkedHashSet<DependencyNode>();

    private Artifact latestArtifact;

    private ArtifactResult latestResult;

    private final LinkedHashSet<RemoteRepository> remoteRepositories = new LinkedHashSet<RemoteRepository>();

    private final Map<String, ArtifactResult> results = new HashMap<String, ArtifactResult>();

    private String projectId;

    // private final DependencyGraphTracker graph;

    public DependencyTracker( final DependencyNode node, final DependencyGraphTracker graph )
    {
        // this.graph = graph;
        nodes.add( node );
        if ( node.getRepositories() != null )
        {
            remoteRepositories.addAll( node.getRepositories() );
        }

        if ( node.getDependency() != null )
        {
            latestArtifact = node.getDependency().getArtifact();
            projectId = projectId( latestArtifact );
        }
    }

    public DependencyTracker( final Artifact artifact, final DependencyGraphTracker graph )
    {
        // this.graph = graph;
        projectId = projectId( artifact );
        latestArtifact = artifact;
    }

    static String projectId( final Artifact a )
    {
        return key( a.getGroupId(), a.getArtifactId(), a.getBaseVersion() );
    }

    public void addParentTrail( final List<MavenProject> descendants )
    {
        final List<String> trail = new ArrayList<String>( descendants.size() );
        for ( final MavenProject project : descendants )
        {
            trail.add( key( project.getGroupId(), project.getArtifactId(), project.getVersion() ) );
        }

        // parentTrails.add( trail );
    }

    public void merge( final DependencyNode node )
    {
        nodes.add( node );
        if ( node.getRepositories() != null )
        {
            remoteRepositories.addAll( node.getRepositories() );
        }

        if ( latestArtifact == null && node.getDependency() != null && node.getDependency().getArtifact() != null )
        {
            latestArtifact = node.getDependency().getArtifact();
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

    public synchronized void setResult( final ArtifactResult result )
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
            LOGGER.error( "PANIC: Cannot find artifact extension to file artifact result (project: " + getProjectId()
                            + ": " + result );
        }

        if ( ext != null )
        {
            final ArtifactResult existing = results.get( ext );
            if ( existing != null && existing.getArtifact() != null )
            {
                LOGGER.warn( "PANIC: Result-map already contains result with resolved artifact for: " + ext );
            }
            else
            {
                results.put( ext, result );
                latestResult = result;
            }
        }
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

    public String getProjectId()
    {
        return projectId;
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

    public void logErrors()
    {
        LOGGER.error( renderErrors() );
    }

    private String renderErrors()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append( "Failed to resolve: " ).append( getProjectId() );
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
        builder.append( projectId );
        builder.append( "\n    results=" );
        builder.append( results );
        builder.append( "\n)" );
        return builder.toString();
    }

    public void removeResult( final String ext )
    {
        results.remove( ext );
    }
}
