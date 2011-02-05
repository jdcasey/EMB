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

package org.commonjava.emb.project.graph;

import org.apache.maven.project.MavenProject;
import org.commonjava.emb.graph.SimpleDirectedGraph;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.resolution.ArtifactResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DependencyGraph
    implements Iterable<DepGraphNode>
{

    private static final long serialVersionUID = 1L;

    private final Set<DepGraphRootNode> roots = new LinkedHashSet<DepGraphRootNode>();

    private final SimpleDirectedGraph<DepGraphNode> graph = new SimpleDirectedGraph<DepGraphNode>();

    public DepGraphRootNode addRoot( final DependencyNode root )
    {
        return addRoot( root, null );
    }

    public DepGraphRootNode addRoot( final DependencyNode root, final MavenProject project )
    {
        final DepGraphRootNode newRoot = new DepGraphRootNode( root );
        final DepGraphRootNode rootNode = (DepGraphRootNode) findOrAdd( newRoot );

        if ( rootNode != newRoot )
        {
            rootNode.merge( root );
        }

        roots.add( rootNode );

        if ( project != null )
        {
            rootNode.setProject( project );
        }

        return rootNode;
    }

    public DepGraphNode addNodeResult( final Artifact artifact, final ArtifactResult result )
    {
        final DepGraphNode node = findOrAdd( new DepGraphNode( artifact, false ) );
        node.merge( result );

        return node;
    }

    private DepGraphNode findOrAdd( DepGraphNode node )
    {
        final List<DepGraphNode> nodes = new ArrayList<DepGraphNode>( graph.vertexSet() );
        final int idx = nodes.indexOf( node );
        if ( idx > -1 )
        {
            node = nodes.get( idx );
        }
        else
        {
            graph.addVertex( node );
        }

        return node;
    }

    public DepGraphNode[] addDependency( final DependencyNode parent, final DependencyNode child )
    {
        final DepGraphNode from = findOrAdd( new DepGraphNode( parent ) );

        final DepGraphNode newTo = new DepGraphNode( child );
        final DepGraphNode to = findOrAdd( newTo );

        // if we're reusing an existing node, merge the new info from the child.
        if ( to != newTo )
        {
            to.merge( child );
        }

        graph.connect( from, to );

        return new DepGraphNode[] { from, to };
    }

    public DepGraphNode[] addDependency( final Artifact parent, final Artifact child, final boolean parentPreResolved,
                                         final boolean childPreResolved )
    {
        final DepGraphNode from = findOrAdd( new DepGraphNode( parent, parentPreResolved ) );

        final DepGraphNode newTo = new DepGraphNode( child, childPreResolved );
        final DepGraphNode to = findOrAdd( newTo );

        // if we're reusing an existing node, merge the new info from the child.
        if ( to != newTo )
        {
            to.merge( child );
        }

        graph.connect( from, to );

        return new DepGraphNode[] { from, to };
    }

    public Set<DepGraphRootNode> getRoots()
    {
        return new LinkedHashSet<DepGraphRootNode>( roots );
    }

    @Override
    public Iterator<DepGraphNode> iterator()
    {
        return new LinkedHashSet<DepGraphNode>( graph.vertexSet() ).iterator();
    }

    public int size()
    {
        return graph.vertexSet().size();
    }

    public SimpleDirectedGraph<DepGraphNode> getGraph()
    {
        return graph;
    }

}
