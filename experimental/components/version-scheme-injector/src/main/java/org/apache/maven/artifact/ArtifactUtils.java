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

package org.apache.maven.artifact;

import org.apache.maven.artifact.versioning.VersionRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class ArtifactUtils
{

    public static boolean isSnapshot( final String version )
    {
        if ( version != null )
        {
            if ( version.regionMatches( true, version.length() - Artifact.SNAPSHOT_VERSION.length(),
                                        Artifact.SNAPSHOT_VERSION, 0, Artifact.SNAPSHOT_VERSION.length() ) )
            {
                return true;
            }
            else if ( Artifact.VERSION_FILE_PATTERN.matcher( version ).matches() )
            {
                return true;
            }
        }
        return false;
    }

    public static String toSnapshotVersion( final String version )
    {
        if ( version == null )
        {
            throw new IllegalArgumentException( "version: null" );
        }

        final Matcher m = Artifact.VERSION_FILE_PATTERN.matcher( version );
        if ( m.matches() )
        {
            return m.group( 1 ) + "-" + Artifact.SNAPSHOT_VERSION;
        }
        else
        {
            return version;
        }
    }

    public static String versionlessKey( final Artifact artifact )
    {
        return versionlessKey( artifact.getGroupId(), artifact.getArtifactId() );
    }

    public static String versionlessKey( final String groupId, final String artifactId )
    {
        if ( groupId == null )
        {
            throw new NullPointerException( "groupId is null" );
        }
        if ( artifactId == null )
        {
            throw new NullPointerException( "artifactId is null" );
        }
        return groupId + ":" + artifactId;
    }

    public static String key( final Artifact artifact )
    {
        return key( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() );
    }

    public static String key( final String groupId, final String artifactId, final String version )
    {
        if ( groupId == null )
        {
            throw new NullPointerException( "groupId is null" );
        }
        if ( artifactId == null )
        {
            throw new NullPointerException( "artifactId is null" );
        }
        if ( version == null )
        {
            throw new NullPointerException( "version is null" );
        }

        return groupId + ":" + artifactId + ":" + version;
    }

    public static Map<String, Artifact> artifactMapByVersionlessId( final Collection<Artifact> artifacts )
    {
        final Map<String, Artifact> artifactMap = new LinkedHashMap<String, Artifact>();

        if ( artifacts != null )
        {
            for ( final Artifact artifact : artifacts )
            {
                artifactMap.put( versionlessKey( artifact ), artifact );
            }
        }

        return artifactMap;
    }

    public static Artifact copyArtifactSafe( final Artifact artifact )
    {
        return ( artifact != null ) ? copyArtifact( artifact ) : null;
    }

    public static Artifact copyArtifact( final Artifact artifact )
    {
        VersionRange range = artifact.getVersionRange();

        // For some reason with the introduction of MNG-1577 we have the case in Yoko where a depMan section has
        // something like the following:
        //
        // <dependencyManagement>
        // <dependencies>
        // <!-- Yoko modules -->
        // <dependency>
        // <groupId>org.apache.yoko</groupId>
        // <artifactId>yoko-core</artifactId>
        // <version>${version}</version>
        // </dependency>
        // ...
        //
        // And the range is not set so we'll check here and set it. jvz.

        if ( range == null )
        {
            range = VersionRange.createFromVersion( artifact.getVersion() );
        }

        final DefaultArtifact clone =
            new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), range.cloneOf(), artifact.getScope(),
                                 artifact.getType(), artifact.getClassifier(), artifact.getArtifactHandler(),
                                 artifact.isOptional() );
        clone.setRelease( artifact.isRelease() );
        clone.setResolvedVersion( artifact.getVersion() );
        clone.setResolved( artifact.isResolved() );
        clone.setFile( artifact.getFile() );

        clone.setAvailableVersions( copyList( artifact.getAvailableVersions() ) );
        if ( artifact.getVersion() != null )
        {
            clone.setBaseVersion( artifact.getBaseVersion() );
        }
        clone.setDependencyFilter( artifact.getDependencyFilter() );
        clone.setDependencyTrail( copyList( artifact.getDependencyTrail() ) );
        clone.setDownloadUrl( artifact.getDownloadUrl() );
        clone.setRepository( artifact.getRepository() );

        return clone;
    }

    /** Returns <code>to</code> collection */
    public static <T extends Collection<Artifact>> T copyArtifacts( final Collection<Artifact> from, final T to )
    {
        for ( final Artifact artifact : from )
        {
            to.add( ArtifactUtils.copyArtifact( artifact ) );
        }
        return to;
    }

    public static <K, T extends Map<K, Artifact>> T copyArtifacts( final Map<K, ? extends Artifact> from, final T to )
    {
        if ( from != null )
        {
            for ( final Map.Entry<K, ? extends Artifact> entry : from.entrySet() )
            {
                to.put( entry.getKey(), ArtifactUtils.copyArtifact( entry.getValue() ) );
            }
        }

        return to;
    }

    private static <T> List<T> copyList( final List<T> original )
    {
        List<T> copy = null;

        if ( original != null )
        {
            copy = new ArrayList<T>();

            if ( !original.isEmpty() )
            {
                copy.addAll( original );
            }
        }

        return copy;
    }

}
