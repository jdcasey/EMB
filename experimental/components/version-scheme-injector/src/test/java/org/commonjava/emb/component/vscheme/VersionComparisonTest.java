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

package org.commonjava.emb.component.vscheme;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.commonjava.emb.component.vscheme.VersionComparison;
import org.junit.Test;

import java.util.Locale;

/**
 * Test ComparableVersion.
 *
 * @author <a href="mailto:hboutemy@apache.org">Hervé Boutemy</a>
 * @version $Id$
 * 
 * Forked from maven 3.0-beta-2
 */
@SuppressWarnings( "unchecked" )
public class VersionComparisonTest
{
    @SuppressWarnings( "rawtypes" )
    private Comparable newComparable( final String version )
    {
        return new VersionComparison( version );
    }

    private static final String[] VERSIONS_QUALIFIER = { "1-alpha2snapshot", "1-alpha2", "1-alpha-123", "1-beta-2",
        "1-beta123", "1-m2", "1-m11", "1-rc", "1-cr2", "1-rc123", "1-SNAPSHOT", "1", "1-sp", "1-sp2", "1-sp123",
        "1-abc", "1-def", "1-pom-1", "1-1-snapshot", "1-1", "1-2", "1-123" };

    private static final String[] VERSIONS_NUMBER = { "2.0", "2-1", "2.0.a", "2.0.0.a", "2.0.2", "2.0.123", "2.1.0",
        "2.1-a", "2.1b", "2.1-c", "2.1-1", "2.1.0.1", "2.2", "2.123", "11.a2", "11.a11", "11.b2", "11.b11", "11.m2",
        "11.m11", "11", "11.a", "11b", "11c", "11m" };

    @SuppressWarnings( "rawtypes" )
    private void checkVersionsOrder( final String[] versions )
    {
        final Comparable[] c = new Comparable[versions.length];
        for ( int i = 0; i < versions.length; i++ )
        {
            c[i] = newComparable( versions[i] );
        }

        for ( int i = 1; i < versions.length; i++ )
        {
            final Comparable low = c[i - 1];
            for ( int j = i; j < versions.length; j++ )
            {
                final Comparable high = c[j];
                assertTrue( "expected " + low + " < " + high, low.compareTo( high ) < 0 );
                assertTrue( "expected " + high + " > " + low, high.compareTo( low ) > 0 );
            }
        }
    }

    @SuppressWarnings( "rawtypes" )
    private void checkVersionsEqual( final String v1, final String v2 )
    {
        final Comparable c1 = newComparable( v1 );
        final Comparable c2 = newComparable( v2 );
        assertTrue( "expected " + v1 + " == " + v2, c1.compareTo( c2 ) == 0 );
        assertTrue( "expected " + v2 + " == " + v1, c2.compareTo( c1 ) == 0 );
        assertTrue( "expected same hashcode for " + v1 + " and " + v2, c1.hashCode() == c2.hashCode() );
        assertTrue( "expected " + v1 + ".equals( " + v2 + " )", c1.equals( c2 ) );
        assertTrue( "expected " + v2 + ".equals( " + v1 + " )", c2.equals( c1 ) );
    }

    @SuppressWarnings( "rawtypes" )
    private void checkVersionsOrder( final String v1, final String v2 )
    {
        final Comparable c1 = newComparable( v1 );
        final Comparable c2 = newComparable( v2 );
        assertTrue( "expected " + v1 + " < " + v2, c1.compareTo( c2 ) < 0 );
        assertTrue( "expected " + v2 + " > " + v1, c2.compareTo( c1 ) > 0 );
    }

    @Test
    public void testVersionsQualifier()
    {
        checkVersionsOrder( VERSIONS_QUALIFIER );
    }

    @Test
    public void testVersionsNumber()
    {
        checkVersionsOrder( VERSIONS_NUMBER );
    }

    @Test
    public void testVersionsEqual()
    {
        checkVersionsEqual( "1", "1" );
        checkVersionsEqual( "1", "1.0" );
        checkVersionsEqual( "1", "1.0.0" );
        checkVersionsEqual( "1.0", "1.0.0" );
        checkVersionsEqual( "1", "1-0" );
        checkVersionsEqual( "1", "1.0-0" );
        checkVersionsEqual( "1.0", "1.0-0" );
        // no separator between number and character
        checkVersionsEqual( "1a", "1.a" );
        checkVersionsEqual( "1a", "1-a" );
        checkVersionsEqual( "1a", "1.0-a" );
        checkVersionsEqual( "1a", "1.0.0-a" );
        checkVersionsEqual( "1.0a", "1.0.a" );
        checkVersionsEqual( "1.0.0a", "1.0.0.a" );
        checkVersionsEqual( "1x", "1.x" );
        checkVersionsEqual( "1x", "1-x" );
        checkVersionsEqual( "1x", "1.0-x" );
        checkVersionsEqual( "1x", "1.0.0-x" );
        checkVersionsEqual( "1.0x", "1.0.x" );
        checkVersionsEqual( "1.0.0x", "1.0.0.x" );

        // aliases
        checkVersionsEqual( "1ga", "1" );
        checkVersionsEqual( "1final", "1" );
        checkVersionsEqual( "1cr", "1rc" );

        // special "aliases" a, b and m for alpha, beta and milestone
        checkVersionsEqual( "1a1", "1alpha1" );
        checkVersionsEqual( "1b2", "1beta2" );
        checkVersionsEqual( "1m3", "1milestone3" );

        // case insensitive
        checkVersionsEqual( "1X", "1x" );
        checkVersionsEqual( "1A", "1a" );
        checkVersionsEqual( "1B", "1b" );
        checkVersionsEqual( "1M", "1m" );
        checkVersionsEqual( "1Ga", "1" );
        checkVersionsEqual( "1GA", "1" );
        checkVersionsEqual( "1Final", "1" );
        checkVersionsEqual( "1FinaL", "1" );
        checkVersionsEqual( "1FINAL", "1" );
        checkVersionsEqual( "1Cr", "1Rc" );
        checkVersionsEqual( "1cR", "1rC" );
        checkVersionsEqual( "1m3", "1Milestone3" );
        checkVersionsEqual( "1m3", "1MileStone3" );
        checkVersionsEqual( "1m3", "1MILESTONE3" );
    }

    @Test
    public void testVersionComparing()
    {
        checkVersionsOrder( "1", "2" );
        checkVersionsOrder( "1.5", "2" );
        checkVersionsOrder( "1", "2.5" );
        checkVersionsOrder( "1.0", "1.1" );
        checkVersionsOrder( "1.1", "1.2" );
        checkVersionsOrder( "1.0.0", "1.1" );
        checkVersionsOrder( "1.0.1", "1.1" );
        checkVersionsOrder( "1.1", "1.2.0" );

        checkVersionsOrder( "1.0-alpha-1", "1.0" );
        checkVersionsOrder( "1.0-alpha-1", "1.0-alpha-2" );
        checkVersionsOrder( "1.0-alpha-1", "1.0-beta-1" );

        checkVersionsOrder( "1.0-beta-1", "1.0-SNAPSHOT" );
        checkVersionsOrder( "1.0-SNAPSHOT", "1.0" );
        checkVersionsOrder( "1.0-alpha-1-SNAPSHOT", "1.0-alpha-1" );

        checkVersionsOrder( "1.0", "1.0-1" );
        checkVersionsOrder( "1.0-1", "1.0-2" );
        checkVersionsOrder( "1.0.0", "1.0-1" );

        checkVersionsOrder( "2.0-1", "2.0.1" );
        checkVersionsOrder( "2.0.1-klm", "2.0.1-lmn" );
        checkVersionsOrder( "2.0.1", "2.0.1-xyz" );

        checkVersionsOrder( "2.0.1", "2.0.1-123" );
        checkVersionsOrder( "2.0.1-xyz", "2.0.1-123" );
    }

    @Test
    public void testLocaleIndependent()
    {
        final Locale orig = Locale.getDefault();
        final Locale[] locales = { Locale.ENGLISH, new Locale( "tr" ), Locale.getDefault() };
        try
        {
            for ( final Locale locale : locales )
            {
                Locale.setDefault( locale );
                checkVersionsEqual( "1-abcdefghijklmnopqrstuvwxyz", "1-ABCDEFGHIJKLMNOPQRSTUVWXYZ" );
            }
        }
        finally
        {
            Locale.setDefault( orig );
        }
    }

    @SuppressWarnings( "rawtypes" )
    @Test
    public void testReuse()
    {
        final VersionComparison c1 = new VersionComparison( "1" );
        c1.parseVersion( "2" );

        final Comparable c2 = newComparable( "2" );

        assertEquals( "reused instance should be equivalent to new instance", c1, c2 );
    }
}
