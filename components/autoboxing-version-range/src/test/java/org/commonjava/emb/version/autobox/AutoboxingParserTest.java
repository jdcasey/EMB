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

import static org.commonjava.emb.version.autobox.AutoboxingParser.parseConstraint;
import static org.commonjava.emb.version.autobox.AutoboxingParser.parseRange;
import static org.commonjava.emb.version.autobox.AutoboxingParser.parseVersion;
import static org.commonjava.emb.version.autobox.qual.RedHatQualifiers.INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sonatype.aether.version.InvalidVersionSpecificationException;

public class AutoboxingParserTest
{

    @Test
    public void parseConstraint_AutoboxSnapshotIncluded()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionConstraint constraint =
            parseConstraint( "2-SNAPSHOT", INSTANCE.rebuildIndicator(), INSTANCE.order(), true );

        final AutoboxableVersion snapshot = parseVersion( "2-SNAPSHOT", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        assertTrue( constraint.containsVersion( snapshot ) );
    }

    @Test
    public void parseConstraint_PinnedNonAutobox()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionConstraint constraint =
            parseConstraint( "2", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );

        final AutoboxableVersion excludeLower = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion include = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion excludeUpper = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertFalse( constraint.containsVersion( excludeLower ) );
        assertTrue( constraint.containsVersion( include ) );
        assertFalse( constraint.containsVersion( excludeUpper ) );
    }

    @Test
    public void parseConstraint_PinnedAutobox()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionConstraint constraint =
            parseConstraint( "2", INSTANCE.rebuildIndicator(), INSTANCE.order(), true );

        final AutoboxableVersion excludeLower = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion include = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion includeRebuild =
            parseVersion( "2-REDHAT-9", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion excludeUpper = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertFalse( constraint.containsVersion( excludeLower ) );
        assertTrue( constraint.containsVersion( include ) );
        assertTrue( constraint.containsVersion( includeRebuild ) );
        assertFalse( constraint.containsVersion( excludeUpper ) );
    }

    @Test
    public void parseConstraint_TwoInclusiveNonOverlappingRanges()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionConstraint constraint =
            parseConstraint( "[1,2][4,5]", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );

        final AutoboxableVersion firstLower = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion firstUpper = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion excluded = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion secondLower = parseVersion( "4", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion secondUpper = parseVersion( "5", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( constraint.containsVersion( firstLower ) );
        assertTrue( constraint.containsVersion( firstUpper ) );
        assertFalse( constraint.containsVersion( excluded ) );
        assertTrue( constraint.containsVersion( secondLower ) );
        assertTrue( constraint.containsVersion( secondUpper ) );
    }

    @Test
    public void parseConstraint_TwoRangesExcludingMiddleVersion()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionConstraint constraint =
            parseConstraint( "[1,3)(3,5]", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );

        final AutoboxableVersion firstLower = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion firstUpper = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion excluded = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion secondLower = parseVersion( "4", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion secondUpper = parseVersion( "5", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        // non-autoboxed ranges don't account for rebuilds properly...
        final AutoboxableVersion excludedRebuild =
            parseVersion( "3-REDHAT-1", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( constraint.containsVersion( firstLower ) );
        assertTrue( constraint.containsVersion( firstUpper ) );
        assertFalse( constraint.containsVersion( excluded ) );
        assertTrue( constraint.containsVersion( excludedRebuild ) );
        assertTrue( constraint.containsVersion( secondLower ) );
        assertTrue( constraint.containsVersion( secondUpper ) );
    }

    @Test
    public void parseConstraintAutoboxed_TwoRangesExcludingMiddleVersions()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionConstraint constraint =
            parseConstraint( "[1,3)(3,5]", INSTANCE.rebuildIndicator(), INSTANCE.order(), true );

        final AutoboxableVersion firstLower = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion firstUpper = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion secondLower = parseVersion( "4", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion secondUpper = parseVersion( "5", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        // autoboxed ranges SHOULD capture rebuilds implicitly...
        final AutoboxableVersion excluded = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion excludedRebuild =
            parseVersion( "3-REDHAT-1", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( constraint.containsVersion( firstLower ) );
        assertTrue( constraint.containsVersion( firstUpper ) );
        assertFalse( constraint.containsVersion( excluded ) );
        assertFalse( constraint.containsVersion( excludedRebuild ) );
        assertTrue( constraint.containsVersion( secondLower ) );
        assertTrue( constraint.containsVersion( secondUpper ) );
    }

    @Test
    public void parseRangeAutoboxedSingleVersion_Implied()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range = parseRange( "3", INSTANCE.rebuildIndicator(), INSTANCE.order(), true );

        final AutoboxableVersion version = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        final AutoboxableVersion rebuild =
            parseVersion( "3-REDHAT-99999", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( version + " should be included.", range.containsVersion( version ) );
        assertTrue( rebuild + " should be included.", range.containsVersion( rebuild ) );
    }

    @Test
    public void parseRangeAutoboxedSingleInclusiveVersion()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range = parseRange( "[3]", INSTANCE.rebuildIndicator(), INSTANCE.order(), true );

        final AutoboxableVersion version = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        final AutoboxableVersion rebuild =
            parseVersion( "3-REDHAT-99999", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( version + " should be included.", range.containsVersion( version ) );
        assertTrue( rebuild + " should be included.", range.containsVersion( rebuild ) );
    }

    @Test
    public void parseRangeAutoboxedSimpleExclusive()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range = parseRange( "(1,3)", INSTANCE.rebuildIndicator(), INSTANCE.order(), true );

        final AutoboxableVersion lowermost = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion middle = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion uppermost = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        final AutoboxableVersion upperRebuild =
            parseVersion( "3-REDHAT-1", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertFalse( lowermost + " should NOT be included.", range.containsVersion( lowermost ) );
        assertTrue( middle + " should be included.", range.containsVersion( middle ) );
        assertFalse( uppermost + " should NOT be included.", range.containsVersion( uppermost ) );
        assertFalse( upperRebuild + " should NOT be included.", range.containsVersion( upperRebuild ) );
    }

    @Test
    public void parseRangeAutoboxedSimpleInclusive()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range = parseRange( "[1,3]", INSTANCE.rebuildIndicator(), INSTANCE.order(), true );

        final AutoboxableVersion lowermost = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion middle = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion uppermost = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion upperRebuild =
            parseVersion( "3-REDHAT-99", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( range.containsVersion( lowermost ) );
        assertTrue( range.containsVersion( middle ) );
        assertTrue( range.containsVersion( uppermost ) );
        assertTrue( range.containsVersion( upperRebuild ) );
    }

    @Test
    public void parseRangeSingleVersion()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range = parseRange( "[2]", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );

        final AutoboxableVersion lowermost = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion middle = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion uppermost = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertFalse( range.containsVersion( lowermost ) );
        assertTrue( range.containsVersion( middle ) );
        assertFalse( range.containsVersion( uppermost ) );
    }

    @Test( expected = InvalidVersionSpecificationException.class )
    public void parseRangeSingleVersionRange_NoImplied()
        throws InvalidVersionSpecificationException
    {
        parseRange( "2", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );
    }

    @Test( expected = InvalidVersionSpecificationException.class )
    public void parseRangeSingleVersion_FailWithExclusiveUpperBound()
        throws InvalidVersionSpecificationException
    {
        parseRange( "[3)", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );
    }

    @Test( expected = InvalidVersionSpecificationException.class )
    public void parseRangeSingleVersion_FailWithExclusiveLowerBound()
        throws InvalidVersionSpecificationException
    {
        parseRange( "(3]", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );
    }

    @Test
    public void parseRange_LowerInclusive_UpperExclusive()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range =
            parseRange( "[1,3)", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );

        final AutoboxableVersion lowermost = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion middle = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion uppermost = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( range.containsVersion( lowermost ) );
        assertTrue( range.containsVersion( middle ) );
        assertFalse( range.containsVersion( uppermost ) );
    }

    @Test
    public void parseRangeSimpleInclusive()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range =
            parseRange( "[1,3]", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );

        final AutoboxableVersion lowermost = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion middle = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion uppermost = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertTrue( range.containsVersion( lowermost ) );
        assertTrue( range.containsVersion( middle ) );
        assertTrue( range.containsVersion( uppermost ) );
    }

    @Test
    public void parseRangeSimpleExclusive()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersionRange range =
            parseRange( "(1,3)", INSTANCE.rebuildIndicator(), INSTANCE.order(), false );

        final AutoboxableVersion lowermost = parseVersion( "1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion middle = parseVersion( "2", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        final AutoboxableVersion uppermost = parseVersion( "3", INSTANCE.rebuildIndicator(), INSTANCE.order() );

        assertFalse( range.containsVersion( lowermost ) );
        assertTrue( range.containsVersion( middle ) );
        assertFalse( range.containsVersion( uppermost ) );
    }

    @Test
    public void parseLocalSnapshot()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersion version = parseVersion( "1.0-SNAPSHOT", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        assertTrue( version.isSnapshot() );
    }

    @Test
    public void parseRemoteSnapshot()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersion version =
            parseVersion( "1.0-20100924.131415-1", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        assertTrue( version.isSnapshot() );
    }

    @Test
    public void parseNonSnapshot()
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersion version = parseVersion( "1.0", INSTANCE.rebuildIndicator(), INSTANCE.order() );
        assertFalse( version.isSnapshot() );
    }

    @Test
    public void roundTripSimpleVersion()
        throws InvalidVersionSpecificationException
    {
        final String version = "1";
        assertRoundTrip( version );
    }

    @Test
    public void roundTripTwoPartVersion()
        throws InvalidVersionSpecificationException
    {
        final String version = "1.2";
        assertRoundTrip( version );
    }

    @Test
    public void roundTripTwoPartVersionWithDash()
        throws InvalidVersionSpecificationException
    {
        final String version = "1-2";
        assertRoundTrip( version );
    }

    @Test
    public void roundTripAlphaNumericVersion()
        throws InvalidVersionSpecificationException
    {
        final String version = "f1";
        assertRoundTrip( version );
    }

    @Test
    public void roundTripRebuild()
        throws InvalidVersionSpecificationException
    {
        assertRoundTrip( "1.0-beta-1-redhat-1" );
    }

    @Test
    public void roundTripLocalSnapshot()
        throws InvalidVersionSpecificationException
    {
        final String version = "1.0-SNAPSHOT";
        assertRoundTrip( version );
    }

    @Test
    public void roundTripRemoteSnapshot()
        throws InvalidVersionSpecificationException
    {
        final String version = "1.0-20100922.120456-2";
        assertRoundTrip( version );
    }

    @Test
    public void roundTripBetaLocalSnapshot()
        throws InvalidVersionSpecificationException
    {
        final String version = "1.0-beta-2-SNAPSHOT";
        assertRoundTrip( version );
    }

    private void assertRoundTrip( final String version )
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersion ver = parseVersion( version, INSTANCE.rebuildIndicator(), INSTANCE.order() );
        assertEquals( version, ver.toString() );
    }

}
