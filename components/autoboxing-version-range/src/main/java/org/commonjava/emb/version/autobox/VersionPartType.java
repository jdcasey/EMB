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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public enum VersionPartType
{
    // NOTE: Order is used for sorting versions!
    REMOTE_SNAPSHOT
    {
        @Override
        public Integer compareParts( final String part1, final String part2 )
        {
            final SimpleDateFormat fmt = new SimpleDateFormat( REMOTE_SNAPSHOT_DATE_FORMAT );
            try
            {
                int idx = part1.indexOf( '-' );

                if ( idx >= REMOTE_SNAPSHOT_DATE_FORMAT.length() )
                {
                    final int bn1 = Integer.parseInt( part1.substring( idx + 1 ) );
                    final Date d1 = fmt.parse( part1.substring( 0, idx ) );

                    idx = part2.indexOf( '-' );

                    if ( idx >= REMOTE_SNAPSHOT_DATE_FORMAT.length() )
                    {
                        final int bn2 = Integer.parseInt( part2.substring( idx + 1 ) );
                        final Date d2 = fmt.parse( part2.substring( 0, idx ) );

                        int comp = d1.compareTo( d2 );
                        if ( comp == 0 )
                        {
                            comp = bn1 - bn2;
                        }

                        return comp;
                    }
                }
            }
            catch ( final NumberFormatException e )
            {
                // dunno...bail out.
            }
            catch ( final ParseException e )
            {
                // dunno...bail out.
            }

            return 0;
        }
    },

    LOCAL_SNAPSHOT
    {
        @Override
        public Integer compareParts( final String part1, final String part2 )
        {
            return 0;
        }
    },

    FREE_FORM
    {
        @Override
        public Integer compareParts( final String part1, final String part2 )
        {
            if ( part1 == part2 )
            {
                return 0;
            }
            else if ( part1 == null && part2 != null )
            {
                return -1;
            }

            return part1.compareToIgnoreCase( part2 );
        }
    },

    QUALIFIER
    {
        @Override
        public Integer compareParts( final String part1, final String part2 )
        {
            return null;
        }
    },

    INT
    {
        @Override
        public Integer compareParts( final String part1, final String part2 )
        {
            try
            {
                final int first = Integer.parseInt( part1 );
                final int second = Integer.parseInt( part2 );

                return first - second;
            }
            catch ( final NumberFormatException e )
            {
                // can't compare.
            }

            return null;
        }
    },

    REBUILD
    {
        @Override
        public Integer compareParts( final String part1, final String part2 )
        {
            return 0;
        }
    },

    ;

    private static final List<VersionPartType> typeList = Arrays.asList( VersionPartType.values() );

    public static final String SNAPSHOT_DATE_FORMAT = "yyyyMMdd";

    public static final String SNAPSHOT_TIME_FORMAT = "HHmmss";

    public static final String REMOTE_SNAPSHOT_DATE_FORMAT = SNAPSHOT_DATE_FORMAT + "." + SNAPSHOT_TIME_FORMAT;

    public static final String LOCAL_SNAPSHOT_MARKER = "SNAPSHOT";

    /**
     * Use the natural ordering of the enum values() method to determine the relative ordering of the two given types.
     */
    public static int compare( final VersionPartType one, final VersionPartType two )
    {
        if ( one == two )
        {
            return 0;
        }

        return typeList.indexOf( one ) - typeList.indexOf( two );
    }

    public static VersionPartType partTypeOf( final String part, final String rebuildIndicator,
                                              final List<String> qualifiers )
    {
        boolean isInt = true;
        for ( final char c : part.toCharArray() )
        {
            if ( !Character.isDigit( c ) )
            {
                isInt = false;
                break;
            }
        }

        if ( isInt )
        {
            return INT;
        }
        else if ( part.equalsIgnoreCase( LOCAL_SNAPSHOT_MARKER ) )
        {
            return LOCAL_SNAPSHOT;
        }
        else if ( qualifiers.contains( part.toUpperCase() ) )
        {
            return QUALIFIER;
        }
        else if ( rebuildIndicator.equalsIgnoreCase( part ) )
        {
            return REBUILD;
        }

        return FREE_FORM;
    }

    /**
     * Assuming both parts come from {@link VersionPart} instances sharing the same {@link VersionPartType}.
     * 
     * @param part1
     *            The full, raw version-part from the first {@link VersionPart}.
     * @param part2
     *            The full, raw version-part from the second {@link VersionPart}.
     * 
     * @return Return -N if part1 sorts ahead, +N if part2 sorts ahead, 0 if they are equal, or NULL if the type cannot
     *         do the comparison.
     */
    public abstract Integer compareParts( String part1, String part2 );
}
