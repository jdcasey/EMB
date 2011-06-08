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

package org.commonjava.emb.project.depgraph.collect;

import static org.sonatype.aether.util.artifact.ArtifacIdUtils.toId;

import org.commonjava.emb.graph.DirectedGraph;
import org.sonatype.aether.RepositorySystemSession;
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

class SlimDepGraph
{

    private static final List<Artifact> NO_ARTIFACTS = Collections.emptyList();
    
    private final DepGraph graph;

    private Map<String, Set<Artifact>> relocations = new HashMap<String, Set<Artifact>>();

    private Map<String, Set<Artifact>> aliases = new HashMap<String, Set<Artifact>>();

    private Map<String, Set<RemoteRepository>> repositoryMap = new HashMap<String, Set<RemoteRepository>>();
    
    private DepGraphCache cache;

    SlimDepGraph( RepositorySystemSession session )
    {
        graph = new DepGraph( this );
        cache = new DepGraphCache( session );
    }
    
    synchronized List<DependencyNode> childrenOf( SlimDependencyNode node )
    {
        Collection<SlimDependencyEdge> allEdges = graph.edgesFrom( node );
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

    synchronized void setArtifact( Artifact artifact )
    {
        cache.setArtifact( artifact );
    }

    synchronized Artifact intern( Artifact artifact )
    {
        return cache.intern( artifact );
    }

    synchronized RemoteRepository intern( RemoteRepository repo )
    {
        return cache.intern( repo );
    }

    synchronized List<Artifact> getArtifacts( List<String> ids )
    {
        if ( ids == null )
        {
            return NO_ARTIFACTS;
        }

        List<Artifact> result = new ArrayList<Artifact>( ids.size() );
        for ( String id : ids )
        {
            result.add( cache.getArtifact( id ) );
        }

        return result;
    }

    Artifact getArtifact( String id )
    {
        return cache.getArtifact( id );
    }

    synchronized void addEdge( SlimDependencyEdge edge )
    {
        graph.addEdge( edge );
    }

    SlimDependencyNode getNode( String id )
    {
        SlimDependencyNode node = new SlimDependencyNode( id, this );
        if ( graph.containsVertex( node ) )
        {
            return node;
        }

        return null;
    }

    SlimDependencyNode getNode( Artifact artifact )
    {
        return getNode( toId( artifact ) );
    }

    List<Artifact> getRelocations( String id )
    {
        Set<Artifact> r = relocations.get( id );
        return r == null ? null : new ArrayList<Artifact>( r );
    }

    synchronized void addRelocation( String id, Artifact relocation )
    {
        Set<Artifact> r = relocations.get( id );
        if ( r == null )
        {
            r = new LinkedHashSet<Artifact>();
            relocations.put( id, r );
        }

        r.add( intern( relocation ) );
    }

    synchronized void setRelocations( String id, List<Artifact> relocations )
    {
        if ( relocations == null )
        {
            // this.relocations.remove( id );
            return;
        }

        Set<Artifact> r = new LinkedHashSet<Artifact>();
        for ( Artifact artifact : relocations )
        {
            r.add( intern( artifact ) );
        }

        this.relocations.put( id, r );
    }

    Collection<Artifact> getAliases( String id )
    {
        return aliases.get( id );
    }

    synchronized void addAlias( String id, Artifact alias )
    {
        Set<Artifact> a = aliases.get( id );
        if ( a == null )
        {
            a = new LinkedHashSet<Artifact>();
            aliases.put( id, a );
        }

        a.add( intern( alias ) );
    }

    synchronized void setAliases( String id, Collection<Artifact> aliases )
    {
        if ( aliases == null )
        {
            // this.aliases.remove( id );
            return;
        }

        Set<Artifact> a = new LinkedHashSet<Artifact>();
        for ( Artifact artifact : aliases )
        {
            a.add( intern( artifact ) );
        }

        this.aliases.put( id, a );
    }

    List<RemoteRepository> getRepositories( String id )
    {
        Set<RemoteRepository> repos = repositoryMap.get( id );
        return repos == null ? null : new ArrayList<RemoteRepository>( repos );
    }

    synchronized void setRepositories( String id, List<RemoteRepository> repositories )
    {
        if ( repositories == null )
        {
            // repositoryMap.remove( id );
            return;
        }

        Set<RemoteRepository> repos = new LinkedHashSet<RemoteRepository>();
        for ( RemoteRepository repo : repositories )
        {
            repos.add( intern( repo ) );
        }

        repositoryMap.put( id, repos );
    }
    

    private static final class DepGraph
        extends DirectedGraph<SlimDependencyNode, SlimDependencyEdge>
    {
        public DepGraph( SlimDepGraph owner )
        {
            super( new SlimDependencyEdge.Factory( owner ) );
        }
        
        public Collection<SlimDependencyEdge> edgesFrom( SlimDependencyNode from )
        {
            return getNakedGraph().getOutEdges( from );
        }

        public void addEdge( SlimDependencyEdge edge )
        {
            getNakedGraph().addEdge( edge, edge.getFrom(), edge.getTo() );
        }

        public boolean containsVertex( SlimDependencyNode vertex )
        {
            return getNakedGraph().containsVertex( vertex );
        }
    }
}
