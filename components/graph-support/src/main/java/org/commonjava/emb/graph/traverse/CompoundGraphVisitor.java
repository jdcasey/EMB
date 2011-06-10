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

import edu.uci.ics.jung.graph.Graph;

public final class CompoundGraphVisitor<V, E>
    implements GraphVisitor<V, E>
{
    private final GraphVisitor<V, E>[] visitors;

    public CompoundGraphVisitor( final GraphVisitor<V, E>... visitors )
    {
        this.visitors = visitors;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.graph.traverse.GraphVisitor#traversedEdge(edu.uci.ics.jung.graph.Graph, java.lang.Object)
     */
    @Override
    public boolean traversedEdge( final Graph<V, E> graph, final E edge )
    {
        boolean doContinue = true;
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            doContinue = doContinue && visitor.traversedEdge( graph, edge );

            if ( !doContinue )
            {
                break;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.graph.traverse.GraphVisitor#startedVertexVisit(edu.uci.ics.jung.graph.Graph,
     *      java.lang.Object)
     */
    @Override
    public boolean startedVertexVisit( final Graph<V, E> graph, final V vertex )
    {
        boolean doContinue = true;
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            doContinue = doContinue && visitor.startedVertexVisit( graph, vertex );

            if ( !doContinue )
            {
                break;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.graph.traverse.GraphVisitor#finishedVertexVisit(edu.uci.ics.jung.graph.Graph,
     *      java.lang.Object)
     */
    @Override
    public boolean finishedVertexVisit( final Graph<V, E> graph, final V vertex )
    {
        boolean doContinue = true;
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            doContinue = doContinue && visitor.finishedVertexVisit( graph, vertex );

            if ( !doContinue )
            {
                break;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.graph.traverse.GraphVisitor#skippedVertexVisit(edu.uci.ics.jung.graph.Graph,
     *      java.lang.Object)
     */
    @Override
    public void skippedVertexVisit( final Graph<V, E> graph, final V vertex )
    {
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            visitor.skippedVertexVisit( graph, vertex );
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.graph.traverse.GraphVisitor#skippedEdgeTraversal(edu.uci.ics.jung.graph.Graph,
     *      java.lang.Object)
     */
    @Override
    public void skippedEdgeTraversal( final Graph<V, E> graph, final E edge )
    {
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            visitor.skippedEdgeTraversal( graph, edge );
        }
    }

}