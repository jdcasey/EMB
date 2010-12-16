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
import org.sonatype.aether.resolution.ArtifactResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphTrackingState
    implements Iterable<Map.Entry<MavenProject, DependencyNode>>
{

    private static final Logger LOGGER = Logger.getLogger( GraphTrackingState.class );

    private final Map<MavenProject, DependencyNode> graphRoots = new HashMap<MavenProject, DependencyNode>();

    private final Map<String, DependencyTrackingState> depTrackers = new HashMap<String, DependencyTrackingState>();

    public GraphTrackingState()
    {
    }

    protected synchronized void addDependencyTrackingState( final DependencyTrackingState state )
    {
        if ( !depTrackers.containsKey( state.getProjectId() ) )
        {
            depTrackers.put( state.getProjectId(), state );
        }
    }

    public void addGraphRoot( final MavenProject project, final DependencyNode graphRoot )
    {
        graphRoots.put( project, graphRoot );
    }

    public Map<MavenProject, DependencyNode> getGraphRoots()
    {
        return graphRoots;
    }

    @Override
    public Iterator<Map.Entry<MavenProject, DependencyNode>> iterator()
    {
        return new HashMap<MavenProject, DependencyNode>( graphRoots ).entrySet().iterator();
    }

    public synchronized Set<DependencyTrackingState> getDependencyTrackingStates()
    {
        // if ( LOGGER.isDebugEnabled() )
        // {
        LOGGER./* debug */info( "RETRIEVING " + depTrackers.size() + " tracking states" );
        // }
        return new HashSet<DependencyTrackingState>( depTrackers.values() );
    }

    public DependencyTrackingState getDependencyState( final String groupId, final String artifactId,
                                                       final String version )
    {
        final String key = key( groupId, artifactId, version );
        // if ( LOGGER.isDebugEnabled() )
        // {
        LOGGER./* debug */info( "RETRIEVING tracking state: " + key );
        // }

        final DependencyTrackingState state = depTrackers.get( key );

        return state;
    }

    public synchronized DependencyTrackingState track( final DependencyNode node, final List<DependencyNode> depTrail )
    {
        if ( node == null || node.getDependency() == null || node.getDependency().getArtifact() == null )
        {
            return null;
        }

        final Artifact a = node.getDependency().getArtifact();
        final String key = key( a.getGroupId(), a.getArtifactId(), a.getBaseVersion() );

        // if ( LOGGER.isDebugEnabled() )
        // {
        LOGGER./* debug */info( "TRACKING from graph traversal: " + key );
        // }

        DependencyTrackingState state = depTrackers.get( key );
        if ( state == null )
        {
            state = new DependencyTrackingState( node, depTrail );
            depTrackers.put( key, state );
            // if ( LOGGER.isDebugEnabled() )
            // {
            LOGGER./* debug */info( "ADDED tracking state for: " + key + "; current # of trackers: "
                            + depTrackers.size() );
            // }
        }
        else
        {
            state.merge( node, depTrail );
        }

        return state;
    }

    public synchronized DependencyTrackingState track( final ArtifactResult result )
    {
        Artifact artifact = result.getArtifact();
        if ( artifact == null && result.getRequest() != null )
        {
            artifact = result.getRequest().getArtifact();
        }

        if ( artifact == null )
        {
            return null;
        }

        final String key = key( artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion() );

        // if ( LOGGER.isDebugEnabled() )
        // {
        LOGGER./* debug */info( "TRACKING from resolution: " + key );
        // }

        DependencyTrackingState state = depTrackers.get( key );
        if ( state == null )
        {
            state = new DependencyTrackingState( artifact );
            depTrackers.put( key, state );
        }

        state.setResult( result );

        return state;
    }

    public int size()
    {
        return depTrackers.size();
    }

    public DependencyTrackingState getDependencyTrackingState( final Artifact a )
    {
        final String key = key( a.getGroupId(), a.getArtifactId(), a.getBaseVersion() );
        return depTrackers.get( key );
    }

    public synchronized DependencyTrackingState track( final Artifact a )
    {
        final String key = key( a.getGroupId(), a.getArtifactId(), a.getBaseVersion() );
        DependencyTrackingState state = depTrackers.get( key );
        if ( state == null )
        {
            state = new DependencyTrackingState( a );
            depTrackers.put( key, state );
        }

        return state;
    }

    public DependencyTrackingState getDependencyState( final MavenProject project )
    {
        return getDependencyState( project.getGroupId(), project.getArtifactId(), project.getVersion() );
    }

}
