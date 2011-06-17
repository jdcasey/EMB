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

    private final Map<String, Set<Artifact>> relocations = new HashMap<String, Set<Artifact>>();

    private final Map<String, Set<Artifact>> aliases = new HashMap<String, Set<Artifact>>();

    private final Map<String, Set<RemoteRepository>> repositoryMap = new HashMap<String, Set<RemoteRepository>>();

    private final DepGraphCache cache;

    SlimDepGraph( final RepositorySystemSession session )
    {
        graph = new DepGraph( this );
        cache = new DepGraphCache( session );
    }

    synchronized List<DependencyNode> childrenOf( final SlimDependencyNode node )
    {
        final Collection<SlimDependencyEdge> allEdges = graph.edgesFrom( node );
        final List<DependencyNode> children = new ArrayList<DependencyNode>( allEdges );
        for ( final SlimDependencyEdge edge : allEdges )
        {
            if ( edge.getFrom() == node && edge.getTo() == node )
            {
                children.remove( edge );
            }
        }

        return children;
    }

    synchronized void setArtifact( final Artifact artifact )
    {
        cache.setArtifact( artifact );
    }

    synchronized Artifact intern( final Artifact artifact )
    {
        return cache.intern( artifact );
    }

    synchronized RemoteRepository intern( final RemoteRepository repo )
    {
        return cache.intern( repo );
    }

    synchronized List<Artifact> getArtifacts( final List<String> ids )
    {
        if ( ids == null )
        {
            return NO_ARTIFACTS;
        }

        final List<Artifact> result = new ArrayList<Artifact>( ids.size() );
        for ( final String id : ids )
        {
            result.add( cache.getArtifact( id ) );
        }

        return result;
    }

    Artifact getArtifact( final String id )
    {
        return cache.getArtifact( id );
    }

    synchronized void addEdge( final SlimDependencyEdge edge )
    {
        graph.addEdge( edge );
    }

    SlimDependencyNode getNode( final String id )
    {
        final SlimDependencyNode node = new SlimDependencyNode( id, this );
        if ( graph.containsVertex( node ) )
        {
            return node;
        }

        return null;
    }

    SlimDependencyNode getNode( final Artifact artifact )
    {
        return getNode( toId( artifact ) );
    }

    List<Artifact> getRelocations( final String id )
    {
        final Set<Artifact> r = relocations.get( id );
        return r == null ? null : new ArrayList<Artifact>( r );
    }

    synchronized void addRelocation( final String id, final Artifact relocation )
    {
        Set<Artifact> r = relocations.get( id );
        if ( r == null )
        {
            r = new LinkedHashSet<Artifact>();
            relocations.put( id, r );
        }

        r.add( intern( relocation ) );
    }

    synchronized void setRelocations( final String id, final List<Artifact> relocations )
    {
        if ( relocations == null )
        {
            // this.relocations.remove( id );
            return;
        }

        final Set<Artifact> r = new LinkedHashSet<Artifact>();
        for ( final Artifact artifact : relocations )
        {
            r.add( intern( artifact ) );
        }

        this.relocations.put( id, r );
    }

    Collection<Artifact> getAliases( final String id )
    {
        return aliases.get( id );
    }

    synchronized void addAlias( final String id, final Artifact alias )
    {
        Set<Artifact> a = aliases.get( id );
        if ( a == null )
        {
            a = new LinkedHashSet<Artifact>();
            aliases.put( id, a );
        }

        a.add( intern( alias ) );
    }

    synchronized void setAliases( final String id, final Collection<Artifact> aliases )
    {
        if ( aliases == null )
        {
            // this.aliases.remove( id );
            return;
        }

        final Set<Artifact> a = new LinkedHashSet<Artifact>();
        for ( final Artifact artifact : aliases )
        {
            a.add( intern( artifact ) );
        }

        this.aliases.put( id, a );
    }

    List<RemoteRepository> getRepositories( final String id )
    {
        final Set<RemoteRepository> repos = repositoryMap.get( id );
        return repos == null ? null : new ArrayList<RemoteRepository>( repos );
    }

    synchronized void setRepositories( final String id, final List<RemoteRepository> repositories )
    {
        if ( repositories == null )
        {
            // repositoryMap.remove( id );
            return;
        }

        final Set<RemoteRepository> repos = new LinkedHashSet<RemoteRepository>();
        for ( final RemoteRepository repo : repositories )
        {
            repos.add( intern( repo ) );
        }

        repositoryMap.put( id, repos );
    }

    private static final class DepGraph
        extends DirectedGraph<SlimDependencyNode, SlimDependencyEdge>
    {
        DepGraph( final SlimDepGraph owner )
        {
            super( new SlimDependencyEdge.Factory( owner ) );
        }

        Collection<SlimDependencyEdge> edgesFrom( final SlimDependencyNode from )
        {
            return getNakedGraph().getOutEdges( from );
        }

        void addEdge( final SlimDependencyEdge edge )
        {
            if ( !getNakedGraph().containsVertex( edge.getFrom() ) )
            {
                getNakedGraph().addVertex( edge.getFrom() );
            }
            
            if ( !getNakedGraph().containsVertex( edge.getTo() ) )
            {
                getNakedGraph().addVertex( edge.getTo() );
            }
            
            getNakedGraph().addEdge( edge, edge.getFrom(), edge.getTo() );
        }

        boolean containsVertex( final SlimDependencyNode vertex )
        {
            return getNakedGraph().containsVertex( vertex );
        }

        void removeEdge( final SlimDependencyEdge edge )
        {
            getNakedGraph().removeEdge( edge );
            final SlimDependencyNode to = edge.getTo();

            final Collection<SlimDependencyEdge> in = getNakedGraph().getInEdges( to );
            if ( in == null || in.isEmpty() )
            {
                getNakedGraph().removeVertex( to );
            }
        }
    }

    public void removeEdge( final SlimDependencyEdge edge )
    {
        graph.removeEdge( edge );
    }
}
