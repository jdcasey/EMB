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

package org.commonjava.xaven.support.mavenscm;

import org.commonjava.xaven.PluginGoal;

public class MavenScmPlugin
    extends PluginGoal
{

    public enum Goal
    {
        BRANCH,
        VALIDATE,
        ADD,
        UNEDIT,
        EXPORT,
        BOOTSTRAP,
        CHANGELOG,
        LIST,
        CHECKIN,
        CHECKOUT,
        STATUS,
        UPDATE,
        DIFF,
        UPDATE_SUBPROJECTS,
        EDIT,
        TAG;

        private String goalName;

        private Goal()
        {
        }

        private Goal( final String goalName )
        {
            this.goalName = goalName;
        }

        public String goalName()
        {
            return goalName == null ? name().toLowerCase().replace( '_', '-' ) : goalName;
        }
    }

    private static final String GROUP_ID = "org.apache.maven.plugins";

    private static final String ARTIFACT_ID = "maven-scm-plugin";

    private static final String VERSION = "1.3";

    public MavenScmPlugin( final Goal goal )
    {
        super( GROUP_ID, ARTIFACT_ID, VERSION, goal.goalName() );
    }

    public MavenScmPlugin( final String version, final Goal goal )
    {
        super( GROUP_ID, ARTIFACT_ID, version, goal.goalName() );
    }

}
