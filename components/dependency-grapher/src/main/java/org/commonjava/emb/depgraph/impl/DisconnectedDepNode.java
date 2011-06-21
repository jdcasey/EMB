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

package org.commonjava.emb.depgraph.impl;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class DisconnectedDepNode
    implements DependencyNode
{

    private final DependencyNode delegate;

    DisconnectedDepNode( final DependencyNode delegate )
    {
        this.delegate = delegate;
    }

    @Override
    public List<DependencyNode> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public boolean accept( final DependencyVisitor visitor )
    {
        return false;
    }

    @Override
    public Dependency getDependency()
    {
        return delegate.getDependency();
    }

    @Override
    public void setArtifact( final Artifact artifact )
    {
        delegate.setArtifact( artifact );
    }

    @Override
    public List<Artifact> getRelocations()
    {
        return delegate.getRelocations();
    }

    @Override
    public Collection<Artifact> getAliases()
    {
        return delegate.getAliases();
    }

    @Override
    public VersionConstraint getVersionConstraint()
    {
        return delegate.getVersionConstraint();
    }

    @Override
    public Version getVersion()
    {
        return delegate.getVersion();
    }

    @Override
    public void setScope( final String scope )
    {
        delegate.setScope( scope );
    }

    @Override
    public String getPremanagedVersion()
    {
        return delegate.getPremanagedVersion();
    }

    @Override
    public String getPremanagedScope()
    {
        return delegate.getPremanagedScope();
    }

    @Override
    public List<RemoteRepository> getRepositories()
    {
        return delegate.getRepositories();
    }

    @Override
    public String getRequestContext()
    {
        return delegate.getRequestContext();
    }

    @Override
    public void setRequestContext( final String context )
    {
        delegate.setRequestContext( context );
    }

    @Override
    public Map<Object, Object> getData()
    {
        return delegate.getData();
    }

    @Override
    public void setData( final Object key, final Object value )
    {
        delegate.setData( key, value );
    }

}
