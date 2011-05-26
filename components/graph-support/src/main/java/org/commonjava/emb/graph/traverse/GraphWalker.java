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

package org.commonjava.emb.graph.traverse;

import org.jgrapht.Graph;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.VertexTraversalEvent;

import java.util.LinkedHashSet;
import java.util.Set;

public final class GraphWalker
{

    public static <V, E> LinkedHashSet<V> walkPath( final Graph<V, E> graph, final V start,
                                                    final GraphVisitor<V, E> visitor )
    {
        final LinkedHashSet<V> encounters = new LinkedHashSet<V>();
        encounters.add( start );
        
        walkEdgesOf( graph, start, visitor, encounters );

        return encounters;
    }

    private static <V, E> void walkEdgesOf( final Graph<V, E> graph, final V from, final GraphVisitor<V, E> visitor,
                                            final Set<V> progress )
    {
        visitor.vertexTraversed( new VertexTraversalEvent<V>( graph, from ) );
        for ( final E edge : graph.edgesOf( from ) )
        {
            final V to = graph.getEdgeTarget( edge );

            if ( to.equals( from ) )
            {
                continue;
            }

            visitor.edgeTraversed( new EdgeTraversalEvent<V, E>( graph, edge ) );
            if ( !progress.contains( to ) )
            {
                progress.add( to );
                walkEdgesOf( graph, to, visitor, progress );
            }
            else
            {
                visitor.skippedVertexTraversal( to );
            }
        }
        visitor.vertexFinished( new VertexTraversalEvent<V>( graph, from ) );
    }
}
