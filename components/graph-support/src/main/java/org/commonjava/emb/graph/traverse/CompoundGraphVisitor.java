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

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.VertexTraversalEvent;

public final class CompoundGraphVisitor<V, E>
    extends GraphVisitor<V, E>
{
    private final GraphVisitor<V, E>[] visitors;

    public CompoundGraphVisitor( final GraphVisitor<V, E>... visitors )
    {
        this.visitors = visitors;
    }

    @Override
    public boolean isStopped()
    {
        if ( super.isStopped() )
        {
            return true;
        }

        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            if ( visitor.isStopped() )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void connectedComponentFinished( final ConnectedComponentTraversalEvent e )
    {
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            visitor.connectedComponentFinished( e );
        }
    }

    @Override
    public void connectedComponentStarted( final ConnectedComponentTraversalEvent e )
    {
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            visitor.connectedComponentStarted( e );
        }
    }

    @Override
    public void edgeTraversed( final EdgeTraversalEvent<V, E> e )
    {
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            visitor.edgeTraversed( e );
        }
    }

    @Override
    public void vertexTraversed( final VertexTraversalEvent<V> e )
    {
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            visitor.vertexTraversed( e );
        }
    }

    @Override
    public void vertexFinished( final VertexTraversalEvent<V> e )
    {
        for ( final GraphVisitor<V, E> visitor : visitors )
        {
            visitor.vertexFinished( e );
        }
    }

}