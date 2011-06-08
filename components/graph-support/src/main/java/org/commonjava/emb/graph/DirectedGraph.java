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

package org.commonjava.emb.graph;

import org.commonjava.emb.graph.DirectionalEdge.DirectionalEdgeFactory;
import org.commonjava.emb.graph.output.EdgePrinter;
import org.commonjava.emb.graph.output.GraphPrinter;
import org.commonjava.emb.graph.output.VertexPrinter;
import org.commonjava.emb.graph.traverse.GraphVisitor;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Graphs;

import java.io.PrintWriter;

public class DirectedGraph<V, E extends DirectionalEdge<V>>
    implements GraphManager<V, E>
{

    private static final long serialVersionUID = 1L;

    private DirectedSparseGraph<V, E> graph = new DirectedSparseGraph<V, E>();

    private final DirectionalEdgeFactory<V, E> edgeFactory;

    public DirectedGraph( DirectionalEdgeFactory<V, E> edgeFactory )
    {
        this.edgeFactory = edgeFactory;
    }

    public DirectedGraph<V, E> connect( final V from, final V to )
    {
        E edge = edgeFactory.createEdge( from, to );

        if ( graph.containsEdge( edge ) )
        {
            return this;
        }

        if ( !graph.containsVertex( from ) )
        {
            graph.addVertex( from );
        }

        if ( !graph.containsVertex( to ) )
        {
            graph.addVertex( to );
        }

        graph.addEdge( edge, from, to );

        return this;
    }

    public abstract static class Visitor<T>
        implements GraphVisitor<T, DirectionalEdge<T>>
    {

    }

    public static final class Printer<T>
        extends GraphPrinter<T, DirectionalEdge<T>>
    {

        public Printer( final PrintWriter printWriter )
        {
            super( printWriter );
        }

        public Printer( final boolean printEdges, final PrintWriter printWriter )
        {
            super( printEdges, printWriter );
        }

        public Printer( final VertexPrinter<T> vPrinter, final PrintWriter printWriter )
        {
            super( vPrinter, printWriter );
        }

        public Printer( final EdgePrinter<DirectionalEdge<T>> ePrinter, final PrintWriter printWriter )
        {
            super( ePrinter, printWriter );
        }

        public Printer( final String indent, final VertexPrinter<T> vPrinter,
                        final EdgePrinter<DirectionalEdge<T>> ePrinter, final PrintWriter printWriter )
        {
            super( indent, vPrinter, ePrinter, printWriter );
        }

    }

    @Override
    public Graph<V, E> getManagedGraph()
    {
        return Graphs.unmodifiableDirectedGraph( graph );
    }
    
    protected Graph<V, E> getNakedGraph()
    {
        return graph;
    }
    
}
