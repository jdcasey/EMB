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

package org.commonjava.emb.graph.output;

import org.commonjava.emb.graph.traverse.GraphVisitor;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.VertexTraversalEvent;

import java.util.ArrayList;
import java.util.List;

public class GraphPrinter<V, E>
    extends GraphVisitor<V, E>
{
    private final StringBuilder builder = new StringBuilder();

    private final String indent;

    private int indentCounter = 0;

    private final boolean printEdges;

    private final List<Object> lines = new ArrayList<Object>();

    private final VertexPrinter<V> vPrinter;

    private final EdgePrinter<E> ePrinter;

    public GraphPrinter()
    {
        this( null, false, new VertexPrinter.ToStringPrinter<V>(), new EdgePrinter.ToStringPrinter<E>() );
    }

    public GraphPrinter( final boolean printEdges )
    {
        this( null, printEdges, new VertexPrinter.ToStringPrinter<V>(), new EdgePrinter.ToStringPrinter<E>() );
    }

    public GraphPrinter( final String indent, final boolean printEdges, final VertexPrinter<V> vPrinter,
                         final EdgePrinter<E> ePrinter )
    {
        this.printEdges = printEdges;
        this.vPrinter = vPrinter;
        this.ePrinter = ePrinter;
        this.indent = indent == null ? "  " : indent;
    }

    public GraphPrinter<V, E> clear()
    {
        builder.setLength( 0 );
        indentCounter = 0;
        lines.clear();

        return this;
    }

    @Override
    public String toString()
    {
        return builder.toString();
    }

    private void indentLine()
    {
        for ( int i = 0; i < indentCounter; i++ )
        {
            builder.append( indent );
        }
    }

    @Override
    public void vertexTraversed( final VertexTraversalEvent<V> e )
    {
        if ( vPrinter != null )
        {
            newLine();
            indentLine();

            final V vertex = e.getVertex();

            builder.append( vPrinter.vertexStarted( vertex ) );
            lines.add( vertex );
        }

        indentCounter++;
    }

    @Override
    public void vertexFinished( final VertexTraversalEvent<V> e )
    {
        if ( vPrinter != null )
        {
            final V vertex = e.getVertex();
            final String ending = vPrinter.vertexFinished( vertex );
            if ( ending != null )
            {
                newLine();
                indentLine();

                builder.append( vPrinter.vertexStarted( vertex ) );
                lines.add( "end" );
            }
        }

        indentCounter--;
    }

    @Override
    public void edgeTraversed( final EdgeTraversalEvent<V, E> e )
    {
        if ( printEdges )
        {
            newLine();
            indentLine();

            final E edge = e.getEdge();

            builder.append( '(' ).append( ePrinter.printEdge( edge ) ).append( ')' );
            lines.add( edge );
        }
    }

    private void newLine()
    {
        if ( builder.length() > 0 )
        {
            builder.append( '\n' );
        }

        final int sz = lines.size() + 1;
        builder.append( sz ).append( ":" );
        if ( sz < 100 )
        {
            builder.append( ' ' );
            if ( sz < 10 )
            {
                builder.append( ' ' );
            }
        }
        builder.append( ">" ).append( indentCounter ).append( ' ' );
    }

    @Override
    public void skippedVertexTraversal( final V vertex )
    {
        newLine();
        indentLine();

        final int idx = lines.indexOf( vertex );

        // builder.append( vertex );
        builder.append( "-DUPLICATE- (see line: " ).append( idx + 1 ).append( ")" );

        // we need some placeholder here, without screwing up indexOf() operations for the vertex...
        lines.add( idx );

    }
}
