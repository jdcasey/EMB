/*
 *  Copyright (C) 2011 John Casey.
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

package org.commonjava.emb.project.graph;

import static org.sonatype.aether.util.artifact.ArtifacIdUtils.toId;

import org.commonjava.emb.graph.DirectedGraph;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SlimDepGraph
{
    
    private DirectedGraph<SlimDependencyNode, SlimDependencyEdge> graph =
        new DirectedGraph<SlimDependencyNode, SlimDependencyEdge>( new SlimDependencyEdge.Factory( this ) );
    
    private Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
    
    private List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();

    public List<DependencyNode> childrenOf( SlimDependencyNode node )
    {
        Set<SlimDependencyEdge> allEdges = graph.edgesOf( node );
        List<DependencyNode> children = new ArrayList<DependencyNode>();
        for ( SlimDependencyEdge edge : allEdges )
        {
            if ( edge.getFrom() == node )
            {
                children.add( edge );
            }
        }
        
        return children;
    }
    
    public synchronized void set( Artifact artifact )
    {
        String key = toId( artifact );
        artifacts.put( key, artifact );
    }

    public synchronized String store( Artifact artifact )
    {
        String key = toId( artifact );
        Artifact result = artifacts.get( key );
        if ( result == null )
        {
            artifacts.put( key, artifact );
            result = artifact;
        }
        
        return key;
    }

    public synchronized RemoteRepository intern( RemoteRepository repo )
    {
        int idx = repositories.indexOf( repo );
        
        if ( idx > -1 )
        {
            return repositories.get( idx );
        }
        else
        {
            repositories.add( repo );
            return repo;
        }
    }

    public synchronized List<Artifact> getArtifacts( List<String> ids )
    {
        List<Artifact> result = new ArrayList<Artifact>( ids.size() );
        for ( String id : ids )
        {
            result.add( artifacts.get( id ) );
        }
        
        return result;
    }

    public Artifact getArtfiact( String id )
    {
        return artifacts.get( id );
    }

    public void addEdge( SlimDependencyNode from, SlimDependencyNode to, SlimDependencyEdge edge )
    {
        if ( !graph.containsVertex( from ) )
        {
            graph.addVertex( from );
        }
        
        if ( !graph.containsVertex( to ) )
        {
            graph.addVertex( to );
        }
        
        if ( !graph.containsEdge( edge ) )
        {
            graph.addEdge( from, to, edge );
        }
    }

    public SlimDependencyNode getNode( String key )
    {
        SlimDependencyNode node = new SlimDependencyNode( key, this );
        if ( graph.containsVertex( node ) )
        {
            return node;
        }
        
        return null;
    }

}
