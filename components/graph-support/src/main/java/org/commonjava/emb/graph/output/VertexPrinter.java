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

public interface VertexPrinter<V>
{

    public static final class ToStringPrinter<T>
        extends AbstractVertexPrinter<T>
    {
        @Override
        public String vertexStarted( final T vertex )
        {
            return vertex == null ? "-null-" : vertex.toString();
        }
    };

    public abstract static class AbstractVertexPrinter<T>
        implements VertexPrinter<T>
    {
        @Override
        public String vertexFinished( final T vertex )
        {
            return null;
        }

        @Override
        public String vertexSkipped( final T vertex )
        {
            return null;
        }
    }

    /**
     * Write the header to signal the start of a new vertex traversal. <b>NEVER</b> <code>null</code>.
     */
    String vertexStarted( V vertex );

    String vertexFinished( V vertex );

    String vertexSkipped( V vertex );

}
