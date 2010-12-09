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

@XStreamAlias( "job" )
public class Job
{

    private String name;

    private ProjectSource projectSource;

    public Job( final String name, final ProjectSource projectSource )
    {
        this.name = name;
        this.projectSource = projectSource;
    }

    Job()
    {
    }

    public String getName()
    {
        return name;
    }

    public ProjectSource getProjectSource()
    {
        return projectSource;
    }

    protected void setName( final String name )
    {
        this.name = name;
    }

    protected void setProjectSource( final ProjectSource projectSource )
    {
        this.projectSource = projectSource;
    }

}
