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

import static org.commonjava.emb.version.autobox.VersionPartType.FREE_FORM;
import static org.commonjava.emb.version.autobox.VersionPartType.INT;
import static org.commonjava.emb.version.autobox.VersionPartType.LOCAL_SNAPSHOT;
import static org.commonjava.emb.version.autobox.VersionPartType.QUALIFIER;
import static org.commonjava.emb.version.autobox.VersionPartType.REBUILD;
import static org.commonjava.emb.version.autobox.VersionPartType.compare;
import static org.commonjava.emb.version.autobox.VersionPartType.partTypeOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VersionPartTypeTest
{

    @Test
    public void compare_LOCAL_SNAPSHOT_to_INT()
    {
        assertTrue( compare( LOCAL_SNAPSHOT, INT ) < 0 );
    }

    @Test
    public void compare_INT_to_QUALIFIER()
    {
        assertTrue( compare( INT, QUALIFIER ) > 0 );
    }

    @Test
    public void compare_QUALIFIER_to_FREE_FORM()
    {
        assertTrue( compare( QUALIFIER, FREE_FORM ) > 0 );
    }

    @Test
    public void compare_FREE_FORM_to_REBUILD()
    {
        assertTrue( compare( FREE_FORM, REBUILD ) < 0 );
    }

    @Test
    public void compareParts_LOCAL_SNAPSHOT_alwaysReturnZero()
    {
        final Integer zero = Integer.valueOf( 0 );
        assertEquals( zero, LOCAL_SNAPSHOT.compareParts( null, null ) );
        assertEquals( zero, LOCAL_SNAPSHOT.compareParts( "one", "two" ) );
        assertEquals( zero, LOCAL_SNAPSHOT.compareParts( "1", "two" ) );
        assertEquals( zero, LOCAL_SNAPSHOT.compareParts( "1", "2" ) );
    }

    @Test
    public void compareParts_QUALIFIER_alwaysReturnNull()
    {
        assertNull( QUALIFIER.compareParts( null, null ) );
        assertNull( QUALIFIER.compareParts( "alpha", "alpha" ) );
        assertNull( QUALIFIER.compareParts( "alpha", "beta" ) );
        assertNull( QUALIFIER.compareParts( "a", "milestone" ) );
    }

    @Test
    public void compareParts_INT_returnNullWhenOneOrMoreIsNonNumeric()
    {
        assertNull( INT.compareParts( "one", "2" ) );
        assertNull( INT.compareParts( "one", "two" ) );
        assertNull( INT.compareParts( "1", "two" ) );
        assertNull( INT.compareParts( "1", null ) );
        assertNull( INT.compareParts( null, "2" ) );
    }

    @Test
    public void compareParts_REBUILD_alwaysReturnZero()
    {
        final Integer zero = Integer.valueOf( 0 );
        assertEquals( zero, REBUILD.compareParts( null, null ) );
        assertEquals( zero, REBUILD.compareParts( "one", "two" ) );
        assertEquals( zero, REBUILD.compareParts( "1", "two" ) );
        assertEquals( zero, REBUILD.compareParts( "1", "2" ) );
        assertEquals( zero, REBUILD.compareParts( "REDHAT", "JBOSS" ) );
    }

    @Test
    public void compareParts_FREE_FORM_returnStringComparison()
    {
        assertEquals( Integer.valueOf( -1 ), FREE_FORM.compareParts( "a", "b" ) );
        assertEquals( Integer.valueOf( 0 ), FREE_FORM.compareParts( "a", "a" ) );
        assertEquals( Integer.valueOf( 1 ), FREE_FORM.compareParts( "b", "a" ) );
        assertEquals( Integer.valueOf( 0 ), FREE_FORM.compareParts( "a", "A" ) );
    }

    @Test
    public void partTypeOf_INT()
    {
        assertEquals( INT, partTypeOf( "1", "redhat", new ArrayList<String>() ) );
    }

    @Test
    public void partTypeOf_LOCAL_SNAPSHOT()
    {
        assertEquals( LOCAL_SNAPSHOT, partTypeOf( "SNAPSHOT", "redhat", new ArrayList<String>() ) );
        assertFalse( LOCAL_SNAPSHOT == partTypeOf( "foo", "redhat", new ArrayList<String>() ) );
    }

    @Test
    public void partTypeOf_REBUILD()
    {
        assertEquals( REBUILD, partTypeOf( "redhat", "redhat", new ArrayList<String>() ) );
        assertEquals( REBUILD, partTypeOf( "REDHAT", "redhat", new ArrayList<String>() ) );
        assertFalse( REBUILD == partTypeOf( "foo", "redhat", new ArrayList<String>() ) );
    }

    @Test
    public void partTypeOf_QUALIFIER()
    {
        final List<String> quals = new ArrayList<String>();
        quals.add( "RC" );
        quals.add( "CR" );
        quals.add( "BETA" );
        quals.add( "B" );
        quals.add( "ALPHA" );
        quals.add( "A" );
        quals.add( "MILESTONE" );
        quals.add( "M" );

        assertEquals( QUALIFIER, partTypeOf( "rc", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "cr", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "beta", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "b", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "alpha", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "a", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "milestone", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "m", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "RC", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "CR", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "BETA", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "B", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "ALPHA", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "A", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "MILESTONE", "redhat", quals ) );
        assertEquals( QUALIFIER, partTypeOf( "M", "redhat", quals ) );
        assertFalse( QUALIFIER == partTypeOf( "REDHAT", "redhat", quals ) );
        assertFalse( QUALIFIER == partTypeOf( "SNAPSHOT", "redhat", quals ) );
        assertFalse( QUALIFIER == partTypeOf( "1", "redhat", quals ) );
        assertFalse( QUALIFIER == partTypeOf( "FOO", "redhat", quals ) );
    }

    @Test
    public void partTypeOf_FREE_FORM()
    {
        final List<String> quals = new ArrayList<String>();
        quals.add( "RC" );

        assertEquals( FREE_FORM, partTypeOf( "Foo", "redhat", quals ) );
        assertFalse( FREE_FORM == partTypeOf( "rc", "redhat", quals ) );
        assertFalse( FREE_FORM == partTypeOf( "SNAPSHOT", "redhat", quals ) );
        assertFalse( FREE_FORM == partTypeOf( "1", "redhat", quals ) );
        assertFalse( FREE_FORM == partTypeOf( "REDHAT", "redhat", quals ) );
    }

}
