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
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

public final class GraphIterationUtils
{

    public static <V, E> void foreachDepthFirst( final Graph<V, E> graph, final GraphVisitor<V, E> visitor )
    {
        iterate( new DepthFirstIterator<V, E>( graph ), visitor );
    }

    public static <V, E> void foreachInverseDepthFirst( final Graph<V, E> graph, final GraphVisitor<V, E> visitor )
    {
        iterate( new InverseDepthFirstIterator<V, E>( graph ), visitor );
    }

    private static <V, E> void iterate( final GraphIterator<V, E> it, final GraphVisitor<V, E> visitor )
    {
        it.addTraversalListener( visitor );
        it.setReuseEvents( true );

        while ( !visitor.isStopped() && it.hasNext() )
        {
            it.next();
        }
    }

    private GraphIterationUtils()
    {
    }

}
