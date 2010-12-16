/*
 * Copyright (c) 2010 Red Hat, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see 
 * <http://www.gnu.org/licenses>.
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
