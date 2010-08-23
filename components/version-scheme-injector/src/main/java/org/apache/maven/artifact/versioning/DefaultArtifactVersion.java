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

import org.commonjava.xaven.component.vscheme.VersionScheme;
import org.commonjava.xaven.component.vscheme.VersionSchemeSelector;

import java.util.StringTokenizer;

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
    private Integer majorVersion;

    private Integer minorVersion;

    private Integer incrementalVersion;

    private Integer buildNumber;

    private String qualifier;

    private ComparableVersion comparable;

    public DefaultArtifactVersion( final String version )
    {
        parseVersion( version );
    }

    @Override
    public int hashCode()
    {
        return 11 + comparable.hashCode();
    }

    @Override
    public boolean equals( final Object other )
    {
        if ( this == other )
        {
            return true;
        }

        if ( !( other instanceof ArtifactVersion ) )
        {
            return false;
        }

        return compareTo( other ) == 0;
    }

    public int compareTo( final Object o )
    {
        final DefaultArtifactVersion otherVersion = (DefaultArtifactVersion) o;
        return comparable.compareTo( otherVersion.comparable );
    }

    public int getMajorVersion()
    {
        return majorVersion != null ? majorVersion : 0;
    }

    public int getMinorVersion()
    {
        return minorVersion != null ? minorVersion : 0;
    }

    public int getIncrementalVersion()
    {
        return incrementalVersion != null ? incrementalVersion : 0;
    }

    public int getBuildNumber()
    {
        return buildNumber != null ? buildNumber : 0;
    }

    public String getQualifier()
    {
        return qualifier;
    }

    public final void parseVersion( final String version )
    {
        final VersionScheme scheme = VersionSchemeSelector.getCurrentVersionScheme();
        comparable = scheme.getVersionComparator( version );

        final int index = version.indexOf( "-" );

        String part1;
        String part2 = null;

        if ( index < 0 )
        {
            part1 = version;
        }
        else
        {
            part1 = version.substring( 0, index );
            part2 = version.substring( index + 1 );
        }

        if ( part2 != null )
        {
            try
            {
                if ( ( part2.length() == 1 ) || !part2.startsWith( "0" ) )
                {
                    buildNumber = Integer.valueOf( part2 );
                }
                else
                {
                    qualifier = part2;
                }
            }
            catch ( final NumberFormatException e )
            {
                qualifier = part2;
            }
        }

        if ( ( part1.indexOf( "." ) < 0 ) && !part1.startsWith( "0" ) )
        {
            try
            {
                majorVersion = Integer.valueOf( part1 );
            }
            catch ( final NumberFormatException e )
            {
                // qualifier is the whole version, including "-"
                qualifier = version;
                buildNumber = null;
            }
        }
        else
        {
            boolean fallback = false;

            final StringTokenizer tok = new StringTokenizer( part1, "." );
            try
            {
                majorVersion = getNextIntegerToken( tok );
                if ( tok.hasMoreTokens() )
                {
                    minorVersion = getNextIntegerToken( tok );
                }
                if ( tok.hasMoreTokens() )
                {
                    incrementalVersion = getNextIntegerToken( tok );
                }
                if ( tok.hasMoreTokens() )
                {
                    fallback = true;
                }

                // string tokenzier won't detect these and ignores them
                if ( part1.indexOf( ".." ) >= 0 || part1.startsWith( "." ) || part1.endsWith( "." ) )
                {
                    fallback = true;
                }
            }
            catch ( final NumberFormatException e )
            {
                fallback = true;
            }

            if ( fallback )
            {
                // qualifier is the whole version, including "-"
                qualifier = version;
                majorVersion = null;
                minorVersion = null;
                incrementalVersion = null;
                buildNumber = null;
            }
        }
    }

    private static Integer getNextIntegerToken( final StringTokenizer tok )
    {
        final String s = tok.nextToken();
        if ( ( s.length() > 1 ) && s.startsWith( "0" ) )
        {
            throw new NumberFormatException( "Number part has a leading 0: '" + s + "'" );
        }
        return Integer.valueOf( s );
    }

    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        if ( majorVersion != null )
        {
            buf.append( majorVersion );
        }
        if ( minorVersion != null )
        {
            buf.append( "." );
            buf.append( minorVersion );
        }
        if ( incrementalVersion != null )
        {
            buf.append( "." );
            buf.append( incrementalVersion );
        }
        if ( buildNumber != null )
        {
            buf.append( "-" );
            buf.append( buildNumber );
        }
        else if ( qualifier != null )
        {
            if ( buf.length() > 0 )
            {
                buf.append( "-" );
            }
            buf.append( qualifier );
        }
        return buf.toString();
    }
}
