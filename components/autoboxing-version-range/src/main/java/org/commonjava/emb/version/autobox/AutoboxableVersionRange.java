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
import org.sonatype.aether.version.VersionRange;

public class AutoboxableVersionRange
    implements VersionRange
{

    private final AutoboxableVersion upperBound;

    private final AutoboxableVersion lowerBound;

    private final boolean upperInclusive;

    private final boolean lowerInclusive;

    public AutoboxableVersionRange( final AutoboxableVersion lower, final boolean lowerInclusive,
                                    final AutoboxableVersion upper, final boolean upperInclusive )
    {
        upperBound = upper;
        this.upperInclusive = upperInclusive;
        lowerBound = lower;
        this.lowerInclusive = lowerInclusive;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.version.VersionRange#containsVersion(org.sonatype.aether.version.Version)
     */
    @Override
    public boolean containsVersion( final Version version )
    {
        if ( lowerBound != null )
        {
            final int comp = lowerBound.compareTo( version );
            if ( comp > 0 )
            {
                return false;
            }
            else if ( !lowerInclusive && comp == 0 )
            {
                return false;
            }
        }

        if ( upperBound != null )
        {
            final int comp = upperBound.compareTo( version );
            if ( comp < 0 )
            {
                return false;
            }
            else if ( !upperInclusive && comp == 0 )
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        sb.append( lowerInclusive ? '[' : '(' );

        if ( lowerBound != null )
        {
            sb.append( lowerBound );
        }

        sb.append( ',' );

        if ( upperBound != null )
        {
            sb.append( upperBound );
        }

        sb.append( upperInclusive ? ']' : ')' );

        return sb.toString();
    }

}
