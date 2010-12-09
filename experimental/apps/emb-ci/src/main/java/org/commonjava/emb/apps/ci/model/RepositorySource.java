/*
 *  Copyright (C) 2010 John Casey.
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

package org.commonjava.emb.apps.ci.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "repository-source" )
public class RepositorySource
    implements ProjectSource
{

    private String groupId;

    private String artifactId;

    private String version;

    @XStreamAlias( "url" )
    private String repositoryUrl;

    public RepositorySource( final String groupId, final String artifactId, final String version )
    {
        this( groupId, artifactId, version, null );
    }

    public RepositorySource( final String groupId, final String artifactId, final String version,
                             final String repositoryUrl )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.repositoryUrl = repositoryUrl;
    }

    RepositorySource()
    {
        groupId = null;
        artifactId = null;
        version = null;
        repositoryUrl = null;
    }

    public String getRepositoryUrl()
    {
        return repositoryUrl;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    protected void setGroupId( final String groupId )
    {
        this.groupId = groupId;
    }

    protected void setArtifactId( final String artifactId )
    {
        this.artifactId = artifactId;
    }

    protected void setVersion( final String version )
    {
        this.version = version;
    }

    protected void setRepositoryUrl( final String repositoryUrl )
    {
        this.repositoryUrl = repositoryUrl;
    }
}
