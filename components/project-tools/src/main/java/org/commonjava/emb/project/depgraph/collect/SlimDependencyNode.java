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

package org.commonjava.emb.project.depgraph.collect;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;

import java.util.Collection;
import java.util.List;

class SlimDependencyNode
{

    public static final String UNKNOWN_ROOT_ID = "anonymous-root".intern();

    private final SlimDepGraph graph;

    private final String key;

    SlimDependencyNode( final String key, final SlimDepGraph graph )
    {
        this.key = key;
        this.graph = graph;
    }

    Collection<Artifact> getAliases()
    {
        return graph.getAliases( key );
    }

    void setAliases( final Collection<Artifact> aliases )
    {
        graph.setAliases( key, aliases );
    }

    void addAlias( final Artifact artifact )
    {
        graph.addAlias( key, artifact );
    }

    List<RemoteRepository> getRepositories()
    {
        return graph.getRepositories( key );
    }

    void setRepositories( final List<RemoteRepository> repositories )
    {
        graph.setRepositories( key, repositories );
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
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
        final SlimDependencyNode other = (SlimDependencyNode) obj;
        if ( key == null )
        {
            if ( other.key != null )
            {
                return false;
            }
        }
        else if ( !key.equals( other.key ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "SlimDependencyNode [artifactId=" + key + "]";
    }
}
