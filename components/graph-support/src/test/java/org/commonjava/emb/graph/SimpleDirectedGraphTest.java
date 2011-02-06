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

import org.commonjava.emb.graph.SimpleDirectedGraph.Printer;
import org.commonjava.emb.graph.output.EdgePrinter;
import org.commonjava.emb.graph.traverse.GraphWalker;
import org.junit.Test;

public class SimpleDirectedGraphTest
{

    @Test
    public void printSimpleStringDiGraph()
    {
        final SimpleDirectedGraph<String> graph = new SimpleDirectedGraph<String>();
        graph.connect( "from", "to" ).connect( "to", "onward" );

        final Printer<String> printer =
            new Printer<String>( new EdgePrinter.ToStringPrinter<DirectionalEdge<String>>() );

        GraphWalker.walkPath( graph, "from", printer );

        System.out.println( printer );
    }

}
