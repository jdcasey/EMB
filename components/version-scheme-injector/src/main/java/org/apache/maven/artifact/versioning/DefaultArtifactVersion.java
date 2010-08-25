package org.apache.maven.artifact.versioning;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.commonjava.xaven.component.vscheme.SchemeAwareArtifactVersion;

/**
 * Default implementation of artifact versioning.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * 
 * Forked from maven 3.0-beta-2
 */
public class DefaultArtifactVersion
    implements ArtifactVersion
{

    private final SchemeAwareArtifactVersion version;

    public DefaultArtifactVersion( final String version )
    {
        this.version = new SchemeAwareArtifactVersion( version );
    }

    @Override
    public int hashCode()
    {
        return version.hashCode();
    }

    @Override
    public boolean equals( final Object other )
    {
        return version.equals( other );
    }

    public int compareTo( final Object o )
    {
        return version.compareTo( o );
    }

    public int getMajorVersion()
    {
        return version.getMajorVersion();
    }

    public int getMinorVersion()
    {
        return version.getMinorVersion();
    }

    public int getIncrementalVersion()
    {
        return version.getIncrementalVersion();
    }

    public int getBuildNumber()
    {
        return version.getBuildNumber();
    }

    public String getQualifier()
    {
        return version.getQualifier();
    }

    public final void parseVersion( final String version )
    {
        this.version.parseVersion( version );
    }

    @Override
    public String toString()
    {
        return version.toString();
    }

}
