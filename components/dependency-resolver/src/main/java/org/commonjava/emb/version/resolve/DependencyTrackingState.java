/*
 * Copyright (c) 2010 Red Hat, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see 
 * <http://www.gnu.org/licenses>.
 */

package org.commonjava.emb.version.resolve;

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

public class DependencyTrackingState
    implements Iterable<Throwable>
{

    private static final Logger LOGGER = Logger.getLogger( DependencyTrackingState.class );

    private transient final Set<List<String>> depTrails = new HashSet<List<String>>();

    private transient final Set<List<String>> parentTrails = new HashSet<List<String>>();

    private Artifact latestArtifact;

    private ArtifactResult latestResult;

    private final LinkedHashSet<RemoteRepository> remoteRepositories = new LinkedHashSet<RemoteRepository>();

    private final Map<String, ArtifactResult> results = new HashMap<String, ArtifactResult>();

    private String projectId;

    public DependencyTrackingState( final DependencyNode node, final List<DependencyNode> depTrail )
    {
        depTrails.add( toTrail( depTrail ) );
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

    public DependencyTrackingState( final Artifact artifact )
    {
        projectId = projectId( artifact );
        latestArtifact = artifact;
    }

    private List<String> toTrail( final List<DependencyNode> pt )
    {
        final List<String> trail = new ArrayList<String>( pt.size() );
        for ( final DependencyNode node : pt )
        {
            if ( node.getDependency() == null || node.getDependency().getArtifact() == null )
            {
                trail.add( "[unknown]" );
            }
            else
            {
                trail.add( String.valueOf( node.getDependency().getArtifact() ) );
            }
        }

        return trail;
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

        parentTrails.add( trail );
    }

    public void merge( final DependencyNode node, final List<DependencyNode> depTrail )
    {
        depTrails.add( toTrail( depTrail ) );

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

    public Set<List<String>> getDependencyTrails()
    {
        return depTrails;
    }

    public Set<List<String>> getParentTrails()
    {
        return parentTrails;
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
        sb.append( "\nDependency of:" );

        for ( final List<String> parentTrail : getDependencyTrails() )
        {
            int indents = 0;
            for ( final String node : parentTrail )
            {
                sb.append( "\n" ).append( indents ).append( ": " );
                for ( int i = 0; i < indents; i++ )
                {
                    sb.append( "  " );
                }
                sb.append( node );
                indents++;

                if ( indents > 6 )
                {
                    sb.append( "..." );
                    break;
                }
            }

            sb.append( "\n" );
        }

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
