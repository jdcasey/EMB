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

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ArtifactOnlyDependencyNode
    implements DependencyNode
{

    private final Dependency dep;

    private String preVersion;

    private String preScope;

    private final Map<Object, Object> data = new LinkedHashMap<Object, Object>();

    private String requestContext = "project";

    ArtifactOnlyDependencyNode( final Artifact artifact )
    {
        dep = new Dependency( artifact, null );
    }

    @Override
    public List<DependencyNode> getChildren()
    {
        return Collections.emptyList();
    }

    @Override
    public Dependency getDependency()
    {
        return dep;
    }

    @Override
    public synchronized void setArtifact( final Artifact artifact )
    {
        if ( artifact == null )
        {
            return;
        }
        else if ( preVersion == null )
        {
            preVersion = dep.getArtifact().getVersion();
        }

        dep.setArtifact( artifact );
    }

    @Override
    public List<Artifact> getRelocations()
    {
        return Collections.emptyList();
    }

    @Override
    public Collection<Artifact> getAliases()
    {
        return Collections.emptySet();
    }

    @Override
    public VersionConstraint getVersionConstraint()
    {
        return null;
    }

    @Override
    public Version getVersion()
    {
        return null;
    }

    @Override
    public synchronized void setScope( final String scope )
    {
        if ( scope == null )
        {
            return;
        }

        if ( preScope == null )
        {
            preScope = dep.getScope();
        }

        dep.setScope( scope );
    }

    @Override
    public synchronized String getPremanagedVersion()
    {
        return preVersion == null ? dep.getArtifact().getVersion() : preVersion;
    }

    @Override
    public synchronized String getPremanagedScope()
    {
        return preScope == null ? dep.getScope() : preScope;
    }

    @Override
    public List<RemoteRepository> getRepositories()
    {
        return Collections.emptyList();
    }

    @Override
    public String getRequestContext()
    {
        return requestContext;
    }

    @Override
    public void setRequestContext( final String requestContext )
    {
        this.requestContext = requestContext;
    }

    @Override
    public Map<Object, Object> getData()
    {
        return data;
    }

    @Override
    public void setData( final Object key, final Object value )
    {
        data.put( key, value );
    }

    @Override
    public boolean accept( final DependencyVisitor visitor )
    {
        return false;
    }

}
