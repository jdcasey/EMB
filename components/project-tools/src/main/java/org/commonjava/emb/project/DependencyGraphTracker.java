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
import org.sonatype.aether.resolution.ArtifactResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraphTracker
    implements Iterable<Map.Entry<MavenProject, DependencyNode>>
{

    private static final Logger LOGGER = Logger.getLogger( DependencyGraphTracker.class );

    private final Map<MavenProject, DependencyNode> graphRoots = new HashMap<MavenProject, DependencyNode>();

    private final Map<String, DependencyTracker> depTrackers = new HashMap<String, DependencyTracker>();

    public DependencyGraphTracker()
    {
    }

    protected synchronized void addDependencyTrackingState( final DependencyTracker state )
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

    public synchronized Set<DependencyTracker> getDependencyTrackingStates()
    {
        // if ( LOGGER.isDebugEnabled() )
        // {
        LOGGER./* debug */info( "RETRIEVING " + depTrackers.size() + " tracking states" );
        // }
        return new HashSet<DependencyTracker>( depTrackers.values() );
    }

    public DependencyTracker getDependencyState( final String groupId, final String artifactId,
                                                       final String version )
    {
        final String key = key( groupId, artifactId, version );
        // if ( LOGGER.isDebugEnabled() )
        // {
        LOGGER./* debug */info( "RETRIEVING tracking state: " + key );
        // }

        final DependencyTracker state = depTrackers.get( key );

        return state;
    }

    public synchronized DependencyTracker track( final DependencyNode node, final List<DependencyNode> depTrail )
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

        DependencyTracker state = depTrackers.get( key );
        if ( state == null )
        {
            state = new DependencyTracker( node, depTrail );
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

    public synchronized DependencyTracker track( final ArtifactResult result )
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

        DependencyTracker state = depTrackers.get( key );
        if ( state == null )
        {
            state = new DependencyTracker( artifact );
            depTrackers.put( key, state );
        }

        state.setResult( result );

        return state;
    }

    public int size()
    {
        return depTrackers.size();
    }

    public DependencyTracker getDependencyTrackingState( final Artifact a )
    {
        final String key = key( a.getGroupId(), a.getArtifactId(), a.getBaseVersion() );
        return depTrackers.get( key );
    }

    public synchronized DependencyTracker track( final Artifact a )
    {
        final String key = key( a.getGroupId(), a.getArtifactId(), a.getBaseVersion() );
        DependencyTracker state = depTrackers.get( key );
        if ( state == null )
        {
            state = new DependencyTracker( a );
            depTrackers.put( key, state );
        }

        return state;
    }

    public DependencyTracker getDependencyState( final MavenProject project )
    {
        return getDependencyState( project.getGroupId(), project.getArtifactId(), project.getVersion() );
    }

}
