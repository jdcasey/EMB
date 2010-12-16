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

import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.DASH;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.DOT;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.NONE;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.compare;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.separatorTypeOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionPartSeparatorTypeTest
{

    @Test
    public void compare_DOT_to_DASH()
    {
        assertTrue( compare( DOT, DASH ) < 0 );
    }

    @Test
    public void compare_DASH_to_NONE()
    {
        assertTrue( compare( DASH, NONE ) == 0 );
    }

    @Test
    public void compare_DOT_to_NONE()
    {
        assertTrue( compare( DOT, NONE ) == 0 );
    }

    @Test
    public void separatorTypeOf_DOT()
    {
        assertEquals( DOT, separatorTypeOf( '.' ) );
    }

    @Test
    public void separatorTypeOf_DASH()
    {
        assertEquals( DASH, separatorTypeOf( '-' ) );
    }

    @Test
    public void separatorTypeOf_NONE()
    {
        assertEquals( NONE, separatorTypeOf( '_' ) );
        assertEquals( NONE, separatorTypeOf( '\n' ) );
        assertEquals( NONE, separatorTypeOf( '|' ) );
        assertEquals( NONE, separatorTypeOf( ':' ) );
        assertEquals( NONE, separatorTypeOf( 'A' ) );
        assertEquals( NONE, separatorTypeOf( '1' ) );
        assertEquals( NONE, separatorTypeOf( '\u0000' ) );
    }

}
