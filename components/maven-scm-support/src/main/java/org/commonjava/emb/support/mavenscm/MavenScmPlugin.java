/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.support.mavenscm;

import org.commonjava.emb.PluginGoal;

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
