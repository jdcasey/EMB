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

@XStreamAlias( "scm-source" )
public class SCMSource
    implements ProjectSource
{

    @XStreamAlias( "url" )
    private String scmUrl;

    private String rootPomPath = "pom.xml";

    public SCMSource( final String scmUrl )
    {
        this( scmUrl, null );
    }

    public SCMSource( final String scmUrl, final String rootPomPath )
    {
        this.scmUrl = scmUrl;
        if ( rootPomPath != null )
        {
            this.rootPomPath = rootPomPath;
        }
    }

    SCMSource()
    {
    }

    protected void setScmUrl( final String scmUrl )
    {
        this.scmUrl = scmUrl;
    }

    protected void setRootPomPath( final String rootPomPath )
    {
        this.rootPomPath = rootPomPath;
    }

    public String getScmUrl()
    {
        return scmUrl;
    }

    public String getRootPomPath()
    {
        return rootPomPath;
    }

}
