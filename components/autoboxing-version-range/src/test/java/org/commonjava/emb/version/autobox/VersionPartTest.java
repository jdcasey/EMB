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
import static org.commonjava.emb.version.autobox.VersionPartType.FREE_FORM;
import static org.commonjava.emb.version.autobox.VersionPartType.INT;
import static org.commonjava.emb.version.autobox.VersionPartType.LOCAL_SNAPSHOT;
import static org.commonjava.emb.version.autobox.VersionPartType.QUALIFIER;
import static org.commonjava.emb.version.autobox.VersionPartType.REBUILD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VersionPartTest
{

    @Test
    public void compare_sameParts_differentSeparators()
    {
        final VersionPart p1 = new VersionPart( INT, "1", DOT, '.', new ArrayList<String>() );
        final VersionPart p2 = new VersionPart( INT, "1", DASH, '-', new ArrayList<String>() );
        final VersionPart p3 = new VersionPart( INT, "1", NONE, null, new ArrayList<String>() );
        final VersionPart p4 = new VersionPart( INT, "1", DOT, '.', new ArrayList<String>() );

        assertTrue( p1.compareTo( p2 ) < 0 );
        assertTrue( p2.compareTo( p3 ) == 0 );
        assertTrue( p3.compareTo( p1 ) == 0 );
        assertTrue( p1.compareTo( p4 ) == 0 );
    }

    @Test
    public void compare_differentPartTypes()
    {
        final VersionPart p3 = new VersionPart( QUALIFIER, "1", DOT, '.', new ArrayList<String>() );
        final VersionPart p1 = new VersionPart( INT, "1", DOT, '.', new ArrayList<String>() );
        final VersionPart p2 = new VersionPart( FREE_FORM, "1", DOT, '.', new ArrayList<String>() );
        final VersionPart p4 = new VersionPart( LOCAL_SNAPSHOT, "1", DOT, '.', new ArrayList<String>() );
        final VersionPart p5 = new VersionPart( REBUILD, "1", DOT, '.', new ArrayList<String>() );
        final VersionPart p6 = new VersionPart( INT, "1", DOT, '.', new ArrayList<String>() );

        assertTrue( p4.compareTo( p1 ) < 0 );
        assertTrue( p1.compareTo( p3 ) > 0 );
        assertTrue( p3.compareTo( p2 ) > 0 );
        assertTrue( p2.compareTo( p5 ) < 0 );
        assertTrue( p1.compareTo( p6 ) == 0 );
    }

    @Test
    public void compare_usesValue()
    {
        VersionPart p1 = new VersionPart( INT, "1", DOT, '.', new ArrayList<String>() );
        VersionPart p2 = new VersionPart( INT, "2", DOT, '.', new ArrayList<String>() );

        assertTrue( p1.compareTo( p2 ) < 0 );

        p1 = new VersionPart( FREE_FORM, "a", DOT, '.', new ArrayList<String>() );
        p2 = new VersionPart( FREE_FORM, "b", DOT, '.', new ArrayList<String>() );

        assertTrue( p1.compareTo( p2 ) < 0 );
    }

    @Test
    public void compare_usesQualifierOrdering()
    {
        final List<String> qualifiers = new ArrayList<String>();
        qualifiers.add( "alpha" );
        qualifiers.add( "beta" );

        final VersionPart p1 = new VersionPart( QUALIFIER, "beta", DASH, '-', qualifiers );
        final VersionPart p2 = new VersionPart( QUALIFIER, "alpha", DASH, '-', qualifiers );
        final VersionPart p3 = new VersionPart( QUALIFIER, "BETA", DASH, '-', qualifiers );

        assertTrue( p1.compareTo( p2 ) > 0 );
        assertEquals( 0, p1.compareTo( p3 ) );
    }

    @Test
    public void toString_reproduceSeparator()
    {
        final VersionPart p1 = new VersionPart( INT, "1", DOT, '.', new ArrayList<String>() );
        final VersionPart p2 = new VersionPart( INT, "1", DASH, '-', new ArrayList<String>() );
        final VersionPart p3 = new VersionPart( INT, "1", NONE, null, new ArrayList<String>() );

        assertEquals( ".1", p1.toString() );
        assertEquals( "-1", p2.toString() );
        assertEquals( "1", p3.toString() );
    }

}
