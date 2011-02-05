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

import org.jgrapht.EdgeFactory;

public class DirectionalEdge<V>
{

    private final V from;

    private final V to;

    public DirectionalEdge( final V from, final V to )
    {
        this.from = from;
        this.to = to;
    }

    public V getFrom()
    {
        return from;
    }

    public V getTo()
    {
        return to;
    }

    public static final class DirectionalEdgeFactory<V>
        implements EdgeFactory<V, DirectionalEdge<V>>
    {
        @Override
        public DirectionalEdge<V> createEdge( final V from, final V to )
        {
            return new DirectionalEdge<V>( from, to );
        }

    }

    @Override
    public int hashCode()
    {
        final int prime = 37;
        int result = 1;
        result = prime * result + ( ( from == null ) ? 0 : from.hashCode() );
        result = prime * result + ( ( to == null ) ? 0 : to.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final DirectionalEdge<?> other = (DirectionalEdge<?>) obj;
        if ( from == null )
        {
            if ( other.from != null )
            {
                return false;
            }
        }
        else if ( !from.equals( other.from ) )
        {
            return false;
        }
        if ( to == null )
        {
            if ( other.to != null )
            {
                return false;
            }
        }
        else if ( !to.equals( other.to ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append( "DirectionalEdge [" ).append( from ).append( " --> " ).append( to ).append( "]" );
        return builder.toString();
    }

}
