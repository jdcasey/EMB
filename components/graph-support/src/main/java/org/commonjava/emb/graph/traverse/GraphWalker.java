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

import org.commonjava.emb.graph.GraphManager;

import edu.uci.ics.jung.graph.Graph;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class GraphWalker
{

    public static <V, E> List<V> walkDepthFirst( final GraphManager<V, E> graphManager, final V start,
                                                 final GraphVisitor<V, E> visitor )
    {
        return walkDepthFirst( graphManager.getManagedGraph(), start, visitor );
    }

    public static <V, E> List<V> walkDepthFirst( final Graph<V, E> graph, final V start,
                                                 final GraphVisitor<V, E> visitor )
    {
        final LinkedHashSet<V> encounters = new LinkedHashSet<V>();
        encounters.add( start );

        walkDepthFirst( graph, start, visitor, encounters );

        return new ArrayList<V>( encounters );
    }

    private static <V, E> void walkDepthFirst( final Graph<V, E> graph, final V from, final GraphVisitor<V, E> visitor,
                                               final Set<V> progress )
    {
        if ( !visitor.startedVertexVisit( graph, from ) )
        {
            return;
        }

        for ( final E edge : graph.getOutEdges( from ) )
        {
            final V to = graph.getOpposite( from, edge );

            if ( to.equals( from ) )
            {
                visitor.skippedVertexVisit( graph, to );
                continue;
            }

            if ( !visitor.traversedEdge( graph, edge ) )
            {
                visitor.skippedEdgeTraversal( graph, edge );
                continue;
            }

            if ( !progress.contains( to ) )
            {
                progress.add( to );
                walkDepthFirst( graph, to, visitor, progress );
            }
            else
            {
                visitor.skippedVertexVisit( graph, to );
            }
        }

        visitor.finishedVertexVisit( graph, from );
    }
}
