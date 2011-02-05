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
import org.jgrapht.traverse.CrossComponentIterator;

import java.util.ArrayList;
import java.util.List;

public class InverseDepthFirstIterator<V, E>
    extends CrossComponentIterator<V, E, InverseDepthFirstIterator.VisitColor>
{

    /**
     * Standard vertex visit state enumeration.
     */
    protected static enum VisitColor
    {
        /**
         * Vertex has not been returned via iterator yet.
         */
        WHITE,

        /**
         * Vertex has been returned via iterator, but we're not done with all of its out-edges yet.
         */
        GRAY,

        /**
         * Vertex has been returned via iterator, and we're done with all of its out-edges.
         */
        BLACK
    }

    // ~ Instance fields --------------------------------------------------------

    /**
     * LIFO stack of vertices which have been encountered but not yet visited (WHITE). This stack also contains
     * <em>sentinel</em> entries representing vertices which have been visited but are still GRAY. A sentinel entry is a
     * sequence (v, null), whereas a non-sentinel entry is just (v).
     */
    private final List<V> stack = new ArrayList<V>();

    // ~ Constructors -----------------------------------------------------------

    /**
     * Creates a new depth-first iterator for the specified graph.
     * 
     * @param g
     *            the graph to be iterated.
     */
    public InverseDepthFirstIterator( final Graph<V, E> g )
    {
        this( g, null );
    }

    /**
     * Creates a new depth-first iterator for the specified graph. Iteration will start at the specified start vertex
     * and will be limited to the connected component that includes that vertex. If the specified start vertex is
     * <code>null</code>, iteration will start at an arbitrary vertex and will not be limited, that is, will be able to
     * traverse all the graph.
     * 
     * @param g
     *            the graph to be iterated.
     * @param startVertex
     *            the vertex iteration to be started.
     */
    public InverseDepthFirstIterator( final Graph<V, E> g, final V startVertex )
    {
        super( g, startVertex );
    }

    // ~ Methods ----------------------------------------------------------------

    /**
     * @see CrossComponentIterator#isConnectedComponentExhausted()
     */
    @Override
    protected boolean isConnectedComponentExhausted()
    {
        for ( ;; )
        {
            if ( stack.isEmpty() )
            {
                return true;
            }
            if ( peekStack() != null )
            {
                // Found a non-sentinel.
                return false;
            }

            // Found a sentinel: pop it, record the finish time,
            // and then loop to check the rest of the stack.

            // Pop null we peeked at above.
            popStack();

            // This will pop corresponding vertex to be recorded as finished.
            recordFinish();
        }
    }

    /**
     * @see CrossComponentIterator#encounterVertex(Object, Object)
     */
    @Override
    protected void encounterVertex( final V vertex, final E edge )
    {
        putSeenData( vertex, VisitColor.WHITE );
        stack.add( vertex );
    }

    /**
     * @see CrossComponentIterator#encounterVertexAgain(Object, Object)
     */
    @Override
    protected void encounterVertexAgain( final V vertex, final E edge )
    {
        final VisitColor color = getSeenData( vertex );
        if ( color != VisitColor.WHITE )
        {
            // We've already visited this vertex; no need to mess with the
            // stack (either it's BLACK and not there at all, or it's GRAY
            // and therefore just a sentinel).
            return;
        }
        final int i = stack.indexOf( vertex );

        // Since we've encountered it before, and it's still WHITE, it
        // *must* be on the stack.
        assert ( i > -1 );
        stack.remove( i );
        stack.add( 0, vertex );
    }

    /**
     * @see CrossComponentIterator#provideNextVertex()
     */
    @Override
    protected V provideNextVertex()
    {
        V v;
        for ( ;; )
        {
            v = popStack();
            if ( v == null )
            {
                // This is a finish-time sentinel we previously pushed.
                recordFinish();
                // Now carry on with another pop until we find a non-sentinel
            }
            else
            {
                // Got a real vertex to start working on
                break;
            }
        }

        // Push a sentinel for v onto the stack so that we'll know
        // when we're done with it.
        stack.add( 0, v );
        stack.add( 0, null );
        putSeenData( v, VisitColor.GRAY );
        return v;
    }

    private V popStack()
    {
        return stack.remove( 0 );
    }

    private V peekStack()
    {
        return stack.get( 0 );
    }

    private void recordFinish()
    {
        final V v = popStack();
        putSeenData( v, VisitColor.BLACK );
        finishVertex( v );
    }
}
