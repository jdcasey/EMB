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

import java.util.Arrays;
import java.util.List;

public enum VersionPartSeparatorType
{

    // NOTE: Order is used for sorting versions!
    DOT,
    DASH,
    NONE;

    private static final List<VersionPartSeparatorType> typeList = Arrays.asList( VersionPartSeparatorType.values() );

    public static int compare( final VersionPartSeparatorType one, final VersionPartSeparatorType two )
    {
        // NONE is a NOP in terms of separator ordering...it cannot sort higher or lower than another type.
        if ( ( one == two ) || ( one == NONE || two == NONE ) )
        {
            return 0;
        }

        return typeList.indexOf( one ) - typeList.indexOf( two );
    }

    public static VersionPartSeparatorType separatorTypeOf( final char c )
    {
        if ( c == '.' )
        {
            return DOT;
        }
        else if ( c == '-' )
        {
            return DASH;
        }

        return NONE;
    }

}
