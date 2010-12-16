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

package org.commonjava.emb.version.autobox;

import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;
import org.sonatype.aether.version.VersionRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AutoboxableVersionConstraint
    implements VersionConstraint
{

    private final List<AutoboxableVersionRange> ranges;

    private final AutoboxableVersion pinnedVersion;

    public AutoboxableVersionConstraint( final List<AutoboxableVersionRange> ranges )
    {
        this.ranges = Collections.unmodifiableList( ranges );
        pinnedVersion = null;
    }

    public AutoboxableVersionConstraint( final AutoboxableVersion pinnedVersion )
    {
        this.pinnedVersion = pinnedVersion;
        ranges = null;
    }

    public AutoboxableVersionConstraint( final AutoboxableVersionRange... ranges )
    {
        this.ranges = new ArrayList<AutoboxableVersionRange>( Arrays.asList( ranges ) );
        pinnedVersion = null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.version.VersionConstraint#containsVersion(org.sonatype.aether.version.Version)
     */
    @Override
    public boolean containsVersion( final Version version )
    {
        if ( pinnedVersion != null )
        {
            return pinnedVersion.compareTo( version ) == 0;
        }

        if ( ranges != null )
        {
            for ( final AutoboxableVersionRange range : ranges )
            {
                if ( range.containsVersion( version ) )
                {
                    return true;
                }
            }

            return false;
        }

        // if pinnedVersion == null && ranges == null, then match anything.
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.version.VersionConstraint#getRanges()
     */
    @Override
    public Collection<VersionRange> getRanges()
    {
        return ranges == null ? null : new ArrayList<VersionRange>( ranges );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.version.VersionConstraint#getVersion()
     */
    @Override
    public Version getVersion()
    {
        return pinnedVersion;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        for ( final AutoboxableVersionRange range : ranges )
        {
            sb.append( range );
        }

        return sb.toString();
    }

}
