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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SlimDepGraph
{
    
    private static final List<Artifact> NO_ARTIFACTS = Collections.emptyList();

    private DirectedGraph<SlimDependencyNode, SlimDependencyEdge> graph =
        new DirectedGraph<SlimDependencyNode, SlimDependencyEdge>( new SlimDependencyEdge.Factory( this ) );
    
    private Map<String, Artifact> artifacts = new HashMap<String, Artifact>();
    
    private List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();

    private Map<String, Set<Artifact>> relocations = new HashMap<String, Set<Artifact>>();

    private Map<String, Set<Artifact>> aliases = new HashMap<String, Set<Artifact>>();

    private Map<String, Set<RemoteRepository>> repositoryMap = new HashMap<String, Set<RemoteRepository>>();

    public synchronized List<DependencyNode> childrenOf( SlimDependencyNode node )
    {
        Set<SlimDependencyEdge> allEdges = graph.edgesOf( node );
        List<DependencyNode> children = new ArrayList<DependencyNode>();
        for ( SlimDependencyEdge edge : allEdges )
        {
            if ( edge.getFrom() == node && edge.getTo() != node )
            {
                children.add( edge );
            }
        }
        
        return children;
    }
    
    public synchronized void setArtifact( Artifact artifact )
    {
        String id = toId( artifact );
        artifacts.put( id, artifact );
    }
    
    public synchronized Artifact intern( Artifact artifact )
    {
        String id = toId( artifact );
        Artifact result = artifacts.get( id );
        if ( result == null )
        {
            artifacts.put( id, artifact );
            result = artifact;
        }
        
        return result;
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
        if ( ids == null )
        {
            return NO_ARTIFACTS;
        }
        
        List<Artifact> result = new ArrayList<Artifact>( ids.size() );
        for ( String id : ids )
        {
            result.add( artifacts.get( id ) );
        }
        
        return result;
    }

    public Artifact getArtifact( String id )
    {
        return artifacts.get( id );
    }

    public void addEdge( SlimDependencyNode from, SlimDependencyNode to, SlimDependencyEdge edge )
    {
        if ( !graph.containsVertex( from ) )
        {
            graph.addVertex( from );
        }
        
        if ( from != to && !graph.containsVertex( to ) )
        {
            graph.addVertex( to );
        }
        
        if ( !graph.containsEdge( edge ) )
        {
            graph.addEdge( from, to, edge );
        }
    }

    public SlimDependencyNode getNode( String id )
    {
        SlimDependencyNode node = new SlimDependencyNode( id, this );
        if ( graph.containsVertex( node ) )
        {
            return node;
        }
        
        return null;
    }

    public SlimDependencyNode getNode( Artifact artifact )
    {
        return getNode( toId( artifact ) );
    }

    public List<Artifact> getRelocations( String id )
    {
        Set<Artifact> r = relocations.get( id );
        return r == null ? null : new ArrayList<Artifact>( r );
    }

    public synchronized void addRelocation( String id, Artifact relocation )
    {
        Set<Artifact> r = relocations.get( id );
        if ( r == null )
        {
            r = new LinkedHashSet<Artifact>();
            relocations.put( id, r );
        }
        
        r.add( intern( relocation ) );
    }

    public synchronized void setRelocations( String id, List<Artifact> relocations )
    {
        if ( relocations == null )
        {
//            this.relocations.remove( id );
            return;
        }
        
        Set<Artifact> r = new LinkedHashSet<Artifact>();
        for ( Artifact artifact : relocations )
        {
            r.add( intern( artifact ) );
        }
        
        this.relocations.put( id, r );
    }

    public Collection<Artifact> getAliases( String id )
    {
        return aliases.get( id );
    }

    public synchronized void addAlias( String id, Artifact alias )
    {
        Set<Artifact> a = aliases.get( id );
        if ( a == null )
        {
            a = new LinkedHashSet<Artifact>();
            aliases.put( id, a );
        }
        
        a.add( intern( alias ) );
    }

    public synchronized void setAliases( String id, Collection<Artifact> aliases )
    {
        if ( aliases == null )
        {
//            this.aliases.remove( id );
            return;
        }
        
        Set<Artifact> a = new LinkedHashSet<Artifact>();
        for ( Artifact artifact : aliases )
        {
            a.add( intern( artifact ) );
        }
        
        this.aliases.put( id, a );
    }

    public List<RemoteRepository> getRepositories( String id )
    {
        Set<RemoteRepository> repos = repositoryMap.get( id );
        return repos == null ? null : new ArrayList<RemoteRepository>( repos );
    }

    public synchronized void setRepositories( String id, List<RemoteRepository> repositories )
    {
        if ( repositories == null )
        {
//            repositoryMap.remove( id );
            return;
        }
        
        Set<RemoteRepository> repos = new LinkedHashSet<RemoteRepository>();
        for ( RemoteRepository repo : repositories )
        {
            repos.add( intern( repo ) );
        }
        
        repositoryMap.put( id, repos );
    }
}
