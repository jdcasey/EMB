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

import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.NONE;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.separatorTypeOf;
import static org.commonjava.emb.version.autobox.VersionPartType.LOCAL_SNAPSHOT;
import static org.commonjava.emb.version.autobox.VersionPartType.REMOTE_SNAPSHOT;
import static org.commonjava.emb.version.autobox.VersionPartType.SNAPSHOT_DATE_FORMAT;
import static org.commonjava.emb.version.autobox.VersionPartType.SNAPSHOT_TIME_FORMAT;
import static org.commonjava.emb.version.autobox.VersionPartType.partTypeOf;

import org.sonatype.aether.version.InvalidVersionSpecificationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public final class AutoboxingParser
{

    private AutoboxingParser()
    {
    }

    public static boolean isSingleVersion( final String spec )
    {
        return findRangeBreaks( spec ).isEmpty();
    }

    public static AutoboxableVersionConstraint parseConstraint( final String versionRangeSpec,
                                                                final String rebuildIndicator,
                                                                final String[] qualifiers, final boolean autoBox )
        throws InvalidVersionSpecificationException
    {
        return parseConstraint( versionRangeSpec, rebuildIndicator, Arrays.asList( qualifiers ), autoBox );
    }

    public static AutoboxableVersionConstraint parseConstraint( final String versionRangeSpec,
                                                                final String rebuildIndicator,
                                                                final List<String> qualifiers, final boolean autoBox )
        throws InvalidVersionSpecificationException
    {
        final TreeSet<Integer> rangeBreaks = findRangeBreaks( versionRangeSpec );
        if ( rangeBreaks.isEmpty() )
        {
            if ( autoBox )
            {
                return new AutoboxableVersionConstraint( parseRange( versionRangeSpec, rebuildIndicator, qualifiers,
                                                                     autoBox ) );
            }
            else
            {
                return new AutoboxableVersionConstraint( parseVersion( versionRangeSpec, rebuildIndicator, qualifiers ) );
            }
        }

        final List<AutoboxableVersionRange> ranges = new ArrayList<AutoboxableVersionRange>();

        int lastIdx = 0;
        for ( final Integer brk : rangeBreaks )
        {
            final String rawRange = versionRangeSpec.substring( lastIdx, brk );
            ranges.add( parseRange( rawRange, rebuildIndicator, qualifiers, autoBox ) );

            lastIdx = brk;
        }

        if ( lastIdx < versionRangeSpec.length() )
        {
            ranges.add( parseRange( versionRangeSpec.substring( lastIdx ), rebuildIndicator, qualifiers, autoBox ) );
        }

        return new AutoboxableVersionConstraint( ranges );
    }

    public static AutoboxableVersionRange parseRange( final String range, final String rebuildIndicator,
                                                      final String[] qualifiers, final boolean autoBox )
        throws InvalidVersionSpecificationException
    {
        return parseRange( range, rebuildIndicator, Arrays.asList( qualifiers ), autoBox );
    }

    public static AutoboxableVersionRange parseRange( final String range, final String rebuildIndicator,
                                                      final List<String> qualifiers, boolean autoBox )
        throws InvalidVersionSpecificationException
    {
        if ( range == null || ( !autoBox && range.length() < 3 ) || ( autoBox && range.length() < 1 ) )
        {
            throw new InvalidVersionSpecificationException( range, "'" + range
                            + "': Version range must be delimited by brackets,"
                            + " and contain either one or two valid versions within." );
        }

        String rawRange = range;
        final char start = range.charAt( 0 );
        boolean lowerInclusive = true;
        switch ( start )
        {
            case '(':
            {
                lowerInclusive = false;
            }
            case '[':
            {
                rawRange = rawRange.substring( 1 );
                break;
            }
            default:
            {
                if ( !autoBox )
                {
                    throw new InvalidVersionSpecificationException( range, "'" + range
                                    + "': Version range is not delimited on the lower boundary." );
                }
            }
        }

        final char end = range.charAt( range.length() - 1 );
        boolean upperInclusive = true;
        switch ( end )
        {
            case ')':
            {
                upperInclusive = false;
            }
            case ']':
            {
                rawRange = rawRange.substring( 0, rawRange.length() - 1 );
                break;
            }
            default:
            {
                if ( !autoBox )
                {
                    throw new InvalidVersionSpecificationException( range, "'" + range
                                    + "': Version range is not delimited on the upper boundary." );
                }
            }
        }

        final String[] versions = rawRange.split( "," );
        if ( versions == null || versions.length < 1 || versions.length > 2 )
        {
            throw new InvalidVersionSpecificationException( range, "'" + range
                            + "': Version range must contain at least one, and at most two, versions." );
        }
        else if ( versions.length == 1 && ( !lowerInclusive || !upperInclusive ) )
        {
            throw new InvalidVersionSpecificationException( range, "'" + range
                            + "': Version range containing exactly one version MUST be inclusive "
                            + "(i.e. using: [version]) on upper and lower bounds." );
        }

        final AutoboxableVersion lowerBase = parseVersion( versions[0], rebuildIndicator, qualifiers );
        if ( lowerBase.isSnapshot() )
        {
            autoBox = false;
        }

        final AutoboxableVersion lowerAutoboxed = lowerBase.createAutoboxUpperBoundVersion();

        // if (2,... then SHIFT to (2-REDHAT-99999,... so we exclude rebuilds of version 2.
        // otherwise, leave it alone so we match version 2 as well as all of its rebuilds.
        final AutoboxableVersion lower = autoBox && !lowerInclusive ? lowerAutoboxed : lowerBase;
        final AutoboxableVersion upper;

        if ( versions.length == 1 )
        {
            upper = !autoBox ? lowerBase : lowerAutoboxed;
        }
        else
        {
            final AutoboxableVersion version = parseVersion( versions[1], rebuildIndicator, qualifiers );
            if ( upperInclusive )
            {
                upper = !autoBox ? version : version.createAutoboxUpperBoundVersion();
            }
            else
            {
                // if ...,3) DO NOT shift to ...,3-REDHAT-99999
                // in this case, ...,3) will also exclude rebuilds of version 3.
                upper = version;
            }
        }

        return new AutoboxableVersionRange( lower, lowerInclusive, upper, upperInclusive );
    }

    public static AutoboxableVersion parseVersion( final String version, final String rebuildIndicator,
                                                   final String[] qualifiers )
        throws InvalidVersionSpecificationException
    {
        return parseVersion( version, rebuildIndicator, Arrays.asList( qualifiers ) );
    }

    public static AutoboxableVersion parseVersion( final String version, final String rebuildIndicator,
                                                   final List<String> qualifiers )
        throws InvalidVersionSpecificationException
    {
        final TreeSet<Integer> partBreaks = findPartBreaks( version );

        final List<VersionPart> parts = new ArrayList<VersionPart>();

        if ( partBreaks.isEmpty() )
        {
            parts.add( parseRawPart( version, rebuildIndicator, qualifiers ) );
        }
        else
        {
            int lastIdx = 0;
            for ( final Integer brk : partBreaks )
            {
                final String rawPart = version.substring( lastIdx, brk );
                parts.add( parseRawPart( rawPart, rebuildIndicator, qualifiers ) );
                lastIdx = brk;
            }

            if ( lastIdx < version.length() )
            {
                parts.add( parseRawPart( version.substring( lastIdx ), rebuildIndicator, qualifiers ) );
            }
        }

        // cull out the nulls, where a part may have been an empty string or something else weird like that.
        for ( final Iterator<VersionPart> it = parts.iterator(); it.hasNext(); )
        {
            final VersionPart part = it.next();
            if ( part == null )
            {
                it.remove();
            }
        }

        final boolean isSnapshot = isSnapshot( parts, qualifiers );
        return new AutoboxableVersion( isSnapshot, rebuildIndicator, qualifiers, parts.toArray( new VersionPart[] {} ) );
    }

    private static boolean isSnapshot( final List<VersionPart> parts, final List<String> qualifiers )
    {
        if ( parts != null && !parts.isEmpty() )
        {
            final VersionPart lastPart = parts.get( parts.size() - 1 );
            if ( lastPart.getType() == LOCAL_SNAPSHOT )
            {
                return true;
            }

            if ( parts.size() > 2 )
            {
                final VersionPart timePart = parts.get( parts.size() - 2 );
                final VersionPart datePart = parts.get( parts.size() - 3 );
                try
                {
                    new SimpleDateFormat( SNAPSHOT_TIME_FORMAT ).parse( timePart.getRawPart() );
                    new SimpleDateFormat( SNAPSHOT_DATE_FORMAT ).parse( datePart.getRawPart() );

                    // remove the last three; we're going to combine them and add one of type == REMOTE_SNAPSHOT.
                    parts.remove( parts.size() - 1 );
                    parts.remove( parts.size() - 1 );
                    parts.remove( parts.size() - 1 );

                    parts.add( new VersionPart( REMOTE_SNAPSHOT, datePart.getRawPart() + timePart + lastPart,
                                                datePart.getSeparatorType(), datePart.getSeparator(), qualifiers ) );

                    return true;
                }
                catch ( final ParseException e )
                {
                    // NOT a snapshot.
                }
            }
        }

        return false;
    }

    /**
     * Parse a version part into one (or more) VersionPart instances. Some things to note for input:
     * 
     * <ol>
     * <li>The rawPart will be prefixed by its separator from the previous part, if there is a separator.</li>
     * <li>The rawPart may in fact be a composite. In which case, the break point(s) will be calculated, and the
     * sub-parts fed back into this method again.</li>
     * </ol>
     * 
     * @param rawPart
     *            The raw version-part string to be parsed into one (or more) VersionPart instances.
     * @param rebuildIndicator
     * @param qualifiers
     * @param parts
     *            The accumulated version-parts from the version being parsed. This is used to accumulate parts, even in
     *            recursive calls.
     */
    private static VersionPart parseRawPart( String rawPart, final String rebuildIndicator,
                                             final List<String> qualifiers )
    {
        if ( rawPart == null || rawPart.trim().length() < 1 )
        {
            return null;
        }

        Character separator = rawPart.charAt( 0 );
        final VersionPartSeparatorType separatorType = separatorTypeOf( separator );
        if ( separatorType != NONE )
        {
            rawPart = rawPart.substring( 1 );
        }
        else
        {
            separator = null;
        }

        int idx = 0;
        while ( idx < rawPart.length() && !Character.isLetterOrDigit( rawPart.charAt( idx ) ) )
        {
            idx++;
        }

        if ( idx > 0 )
        {
            rawPart = rawPart.substring( idx );
        }

        if ( rawPart.length() < 1 )
        {
            return null;
        }

        final VersionPartType type = partTypeOf( rawPart, rebuildIndicator, qualifiers );

        return new VersionPart( type, rawPart, separatorType, separator, qualifiers );
    }

    /**
     * Breaks consist of any non-alphanumeric character, or any alpha-to-numeric or numeric-to-alpha boundary.
     * 
     * @param version
     *            The version to be parsed for breaks.
     * 
     * @return The list of indexes in the version where breaks occur, for feeding into
     *         {@link String#substring(int, int)}. Return an empty set if no breaks are detected. NEVER NULL.
     */
    private static TreeSet<Integer> findPartBreaks( final String version )
    {
        final TreeSet<Integer> breaks = new TreeSet<Integer>();

        char last = '\u0000';

        int idx = 0;
        for ( final char c : version.toCharArray() )
        {
            if ( !Character.isLetterOrDigit( c ) )
            {
                breaks.add( idx );
            }
            else if ( Character.isLetterOrDigit( last )
                            && ( ( Character.isDigit( last ) && !Character.isDigit( c ) ) || ( !Character.isDigit( last ) && Character.isDigit( c ) ) ) )
            {
                breaks.add( idx );
            }

            last = c;
            idx++;
        }

        return breaks;
    }

    /**
     * Breaks consist of any [(,)] character.
     * 
     * @param version
     *            The version-range to be parsed for breaks.
     * 
     * @return The list of indexes in the version-range where breaks occur, for feeding into
     *         {@link String#substring(int, int)}. Return an empty set if no breaks are detected. NEVER NULL.
     */
    private static TreeSet<Integer> findRangeBreaks( final String version )
    {
        final TreeSet<Integer> breaks = new TreeSet<Integer>();

        int idx = 0;
        for ( final char c : version.toCharArray() )
        {
            if ( idx > 0 && ( ( '[' == c ) || ( '(' == c ) ) )
            {
                breaks.add( idx );
            }

            idx++;
        }

        return breaks;
    }

}
