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
