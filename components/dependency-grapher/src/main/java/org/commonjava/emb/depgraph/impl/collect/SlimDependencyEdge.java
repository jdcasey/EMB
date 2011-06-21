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

package org.commonjava.emb.depgraph.impl.collect;

import org.commonjava.emb.graph.DirectionalEdge;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SlimDependencyEdge
    extends DirectionalEdge<SlimDependencyNode>
    implements DependencyNode
{

    private final SlimDepGraph graph;

    private Dependency dependency;

    private String key;

    private VersionConstraint versionConstraint;

    private Version version;

    private String scope;

    private String preManagedVersion;

    private String preManagedScope;

    private String requestContext;

    private Map<Object, Object> data;

    SlimDependencyEdge( final SlimDependencyNode from, final SlimDependencyNode to, final SlimDepGraph graph )
    {
        super( from, to );
        graph.addEdge( this );
        this.graph = graph;
    }

    SlimDependencyEdge( final SlimDependencyNode root, final SlimDepGraph graph )
    {
        super( root, root );
        graph.addEdge( this );
        this.graph = graph;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getChildren()
     */
    @Override
    public List<DependencyNode> getChildren()
    {
        return graph.childrenOf( getTo() );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getDependency()
     */
    @Override
    public Dependency getDependency()
    {
        return dependency == null ? null : dependency.setArtifact( graph.intern( dependency.getArtifact() ) );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#setArtifact(org.sonatype.aether.artifact.Artifact)
     */
    @Override
    public void setArtifact( final Artifact artifact )
    {
        graph.setArtifact( artifact );
        if ( dependency != null )
        {
            dependency = dependency.setArtifact( artifact );
        }
    }

    Artifact getArtifact()
    {
        return graph.getArtifact( key );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getRelocations()
     */
    @Override
    public List<Artifact> getRelocations()
    {
        return graph.getRelocations( key );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getVersionConstraint()
     */
    @Override
    public VersionConstraint getVersionConstraint()
    {
        return versionConstraint;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getVersion()
     */
    @Override
    public Version getVersion()
    {
        return version;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#setScope(java.lang.String)
     */
    @Override
    public void setScope( final String scope )
    {
        this.scope = scope.intern();
    }

    String getScope()
    {
        return scope;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getPremanagedVersion()
     */
    @Override
    public String getPremanagedVersion()
    {
        return preManagedVersion;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getPremanagedScope()
     */
    @Override
    public String getPremanagedScope()
    {
        return preManagedScope;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getRepositories()
     */
    @Override
    public List<RemoteRepository> getRepositories()
    {
        return getTo().getRepositories();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getRequestContext()
     */
    @Override
    public String getRequestContext()
    {
        return requestContext;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#setRequestContext(java.lang.String)
     */
    @Override
    public void setRequestContext( final String requestContext )
    {
        this.requestContext = requestContext.intern();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getData()
     */
    @Override
    public Map<Object, Object> getData()
    {
        return Collections.unmodifiableMap( data );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#setData(java.lang.Object, java.lang.Object)
     */
    @Override
    public synchronized void setData( final Object key, final Object value )
    {
        if ( data == null )
        {
            data = new HashMap<Object, Object>();
        }

        if ( key != null )
        {
            data.put( key, value );
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#accept(org.sonatype.aether.graph.DependencyVisitor)
     */
    @Override
    public boolean accept( final DependencyVisitor visitor )
    {
        if ( visitor.visitEnter( this ) )
        {
            for ( final DependencyNode child : getChildren() )
            {
                if ( !child.accept( visitor ) )
                {
                    break;
                }
            }
        }

        return visitor.visitLeave( this );
    }

    void setDependency( final Dependency dependency )
    {
        this.dependency = dependency == null ? null : dependency.setArtifact( graph.intern( dependency.getArtifact() ) );
    }

    void setRelocations( final List<Artifact> relocations )
    {
        graph.setRelocations( key, relocations );
    }

    void addRelocation( final Artifact relocation )
    {
        graph.addRelocation( key, relocation );
    }

    void setVersionConstraint( final VersionConstraint versionConstraint )
    {
        this.versionConstraint = versionConstraint;
    }

    void setVersion( final Version version )
    {
        this.version = version;
    }

    void setPreManagedVersion( final String preManagedVersion )
    {
        this.preManagedVersion = preManagedVersion;
    }

    void setPreManagedScope( final String preManagedScope )
    {
        this.preManagedScope = preManagedScope;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.graph.DependencyNode#getAliases()
     */
    @Override
    public Collection<Artifact> getAliases()
    {
        return getTo().getAliases();
    }

    static final class Factory
        implements DirectionalEdgeFactory<SlimDependencyNode, SlimDependencyEdge>
    {

        private final SlimDepGraph graph;

        Factory( final SlimDepGraph graph )
        {
            this.graph = graph;
        }

        @Override
        public SlimDependencyEdge createEdge( final SlimDependencyNode from, final SlimDependencyNode to )
        {
            return new SlimDependencyEdge( from, to, graph );
        }

    }

    void setPremanagedScope( final String premanagedScope )
    {
        preManagedScope = premanagedScope;
    }

    void setPremanagedVersion( final String premanagedVersion )
    {
        preManagedVersion = premanagedVersion;
    }
    
    public String toString()
    {
        return "Edge:\n\tFrom: " + getFrom() + "\n\tTo: " + getTo();
    }

}
