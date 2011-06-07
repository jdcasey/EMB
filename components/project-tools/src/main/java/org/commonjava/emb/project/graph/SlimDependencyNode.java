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
import org.sonatype.aether.repository.RemoteRepository;

import java.util.Collection;
import java.util.List;

public class SlimDependencyNode
{

    public static final String UNKNOWN_ROOT_ID = "anonymous-root".intern();

    private SlimDepGraph graph;

    private final String key;

    public SlimDependencyNode( String key, SlimDepGraph graph )
    {
        this.key = key;
        this.graph = graph;
    }

    public Collection<Artifact> getAliases()
    {
        return graph.getAliases( key );
    }

    public synchronized void setAliases( Collection<Artifact> aliases )
    {
        graph.setAliases( key, aliases );
    }

    public synchronized void addAlias( Artifact artifact )
    {
        graph.addAlias( key, artifact );
    }

    public List<RemoteRepository> getRepositories()
    {
        return graph.getRepositories( key );
    }

    public synchronized void setRepositories( List<RemoteRepository> repositories )
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
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        SlimDependencyNode other = (SlimDependencyNode) obj;
        if ( key == null )
        {
            if ( other.key != null )
                return false;
        }
        else if ( !key.equals( other.key ) )
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "SlimDependencyNode [artifactId=" + key + "]";
    }
}
