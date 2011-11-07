/*
 * Copyright 2011 Red Hat, Inc.
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
