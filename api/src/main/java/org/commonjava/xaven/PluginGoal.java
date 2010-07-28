package org.commonjava.xaven;

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

public class PluginGoal
{

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String pluginPrefix;

    private final String goal;

    public PluginGoal( final String groupId, final String artifactId, final String version, final String goal )
    {
        pluginPrefix = null;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.goal = goal;
    }

    public PluginGoal( final String groupId, final String artifactId, final String goal )
    {
        pluginPrefix = null;
        this.groupId = groupId;
        this.artifactId = artifactId;
        version = null;
        this.goal = goal;
    }

    public PluginGoal( final String pluginPrefix, final String goal )
    {
        this.pluginPrefix = pluginPrefix;
        groupId = null;
        artifactId = null;
        version = null;
        this.goal = goal;
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

    public String getPluginPrefix()
    {
        return pluginPrefix;
    }

    public String getGoal()
    {
        return goal;
    }

    public String formatCliGoal()
    {
        final StringBuilder sb = new StringBuilder();
        if ( pluginPrefix != null )
        {
            sb.append( pluginPrefix );
        }
        else
        {
            sb.append( groupId ).append( ':' ).append( artifactId );
            if ( version != null )
            {
                sb.append( ':' ).append( version );
            }
        }
        sb.append( ':' ).append( goal );

        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "Plugin+Goal: " + formatCliGoal();
    }

}
