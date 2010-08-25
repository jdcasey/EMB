package org.commonjava.xaven.component.vscheme;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.Restriction;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Construct a version range from a specification.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SchemeAwareVersionRange
{
    private final VersionScheme scheme;

    private final ArtifactVersion recommendedVersion;

    private final List<Restriction> restrictions;

    private SchemeAwareVersionRange( final ArtifactVersion recommendedVersion, final List<Restriction> restrictions,
                                     final VersionScheme scheme )
    {
        this.recommendedVersion = recommendedVersion;
        this.restrictions = restrictions;
        this.scheme = scheme;
    }

    public ArtifactVersion getRecommendedVersion()
    {
        return recommendedVersion;
    }

    public List<Restriction> getRestrictions()
    {
        return restrictions;
    }

    public SchemeAwareVersionRange cloneOf()
    {
        List<Restriction> copiedRestrictions = null;

        if ( getRestrictions() != null )
        {
            copiedRestrictions = new ArrayList<Restriction>();

            if ( !getRestrictions().isEmpty() )
            {
                copiedRestrictions.addAll( getRestrictions() );
            }
        }

        return new SchemeAwareVersionRange( getRecommendedVersion(), copiedRestrictions, scheme );
    }

    public static SchemeAwareVersionRange from( final VersionRange range, final VersionScheme scheme )
    {
        try
        {
            return createFromVersionSpec( range.toString(), scheme );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            // cannot happen...
        }

        return null;
    }

    public static SchemeAwareVersionRange from( final VersionRange range, final String scheme )
    {
        try
        {
            return createFromVersionSpec( range.toString(), scheme );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            // cannot happen...
        }

        return null;
    }

    /**
     * Create a version range from a string representation
     * <p/>
     * Some spec examples are
     * <ul>
     * <li><code>1.0</code> Version 1.0</li>
     * <li><code>[1.0,2.0)</code> Versions 1.0 (included) to 2.0 (not included)</li>
     * <li><code>[1.0,2.0]</code> Versions 1.0 to 2.0 (both included)</li>
     * <li><code>[1.5,)</code> Versions 1.5 and higher</li>
     * <li><code>(,1.0],[1.2,)</code> Versions up to 1.0 (included) and 1.2 or higher</li>
     * </ul>
     *
     * @param spec string representation of a version or version range
     * @return a new {@link SchemeAwareVersionRange} object that represents the spec
     * @throws InvalidVersionSpecificationException
     *
     */
    public static SchemeAwareVersionRange createFromVersionSpec( final String spec, final String scheme )
        throws InvalidVersionSpecificationException
    {
        final VersionScheme versionScheme = VersionSchemeSelector.getTLVersionScheme( scheme );
        return createFromVersionSpec( spec, versionScheme );
    }

    @SuppressWarnings( "unchecked" )
    public static SchemeAwareVersionRange createFromVersionSpec( final String spec, final VersionScheme scheme )
        throws InvalidVersionSpecificationException
    {
        if ( spec == null )
        {
            return null;
        }

        final List<Restriction> restrictions = new ArrayList<Restriction>();
        String process = spec;
        ArtifactVersion version = null;
        ArtifactVersion upperBound = null;
        ArtifactVersion lowerBound = null;

        while ( process.startsWith( "[" ) || process.startsWith( "(" ) )
        {
            final int index1 = process.indexOf( ")" );
            final int index2 = process.indexOf( "]" );

            int index = index2;
            if ( index2 < 0 || index1 < index2 )
            {
                if ( index1 >= 0 )
                {
                    index = index1;
                }
            }

            if ( index < 0 )
            {
                throw new InvalidVersionSpecificationException( "Unbounded range: " + spec );
            }

            final Restriction restriction = parseRestriction( process.substring( 0, index + 1 ), scheme );
            if ( lowerBound == null )
            {
                lowerBound = restriction.getLowerBound();
            }
            if ( upperBound != null )
            {
                if ( restriction.getLowerBound() == null || restriction.getLowerBound().compareTo( upperBound ) < 0 )
                {
                    throw new InvalidVersionSpecificationException( "Ranges overlap: " + spec );
                }
            }
            restrictions.add( restriction );
            upperBound = restriction.getUpperBound();

            process = process.substring( index + 1 ).trim();

            if ( process.length() > 0 && process.startsWith( "," ) )
            {
                process = process.substring( 1 ).trim();
            }
        }

        if ( process.length() > 0 )
        {
            if ( restrictions.size() > 0 )
            {
                throw new InvalidVersionSpecificationException(
                                                                "Only fully-qualified sets allowed in multiple set scenario: "
                                                                    + spec );
            }
            else
            {
                version = new SchemeAwareArtifactVersion( process, scheme );
                restrictions.add( Restriction.EVERYTHING );
            }
        }

        return new SchemeAwareVersionRange( version, restrictions, scheme );
    }

    @SuppressWarnings( { "unchecked" } )
    private static Restriction parseRestriction( final String spec, final VersionScheme scheme )
        throws InvalidVersionSpecificationException
    {
        final boolean lowerBoundInclusive = spec.startsWith( "[" );
        final boolean upperBoundInclusive = spec.endsWith( "]" );

        final String process = spec.substring( 1, spec.length() - 1 ).trim();

        Restriction restriction;

        final int index = process.indexOf( "," );

        if ( index < 0 )
        {
            if ( !lowerBoundInclusive || !upperBoundInclusive )
            {
                throw new InvalidVersionSpecificationException( "Single version must be surrounded by []: " + spec );
            }

            final ArtifactVersion version = new SchemeAwareArtifactVersion( process, scheme );

            restriction = new Restriction( version, lowerBoundInclusive, version, upperBoundInclusive );
        }
        else
        {
            final String lowerBound = process.substring( 0, index ).trim();
            final String upperBound = process.substring( index + 1 ).trim();
            if ( lowerBound.equals( upperBound ) )
            {
                throw new InvalidVersionSpecificationException( "Range cannot have identical boundaries: " + spec );
            }

            ArtifactVersion lowerVersion = null;
            if ( lowerBound.length() > 0 )
            {
                lowerVersion = new SchemeAwareArtifactVersion( lowerBound, scheme );
            }
            ArtifactVersion upperVersion = null;
            if ( upperBound.length() > 0 )
            {
                upperVersion = new SchemeAwareArtifactVersion( upperBound, scheme );
            }

            if ( upperVersion != null && lowerVersion != null && upperVersion.compareTo( lowerVersion ) < 0 )
            {
                throw new InvalidVersionSpecificationException( "Range defies version ordering: " + spec );
            }

            restriction = new Restriction( lowerVersion, lowerBoundInclusive, upperVersion, upperBoundInclusive );
        }

        return restriction;
    }

    public static SchemeAwareVersionRange createFromVersion( final String version, final String scheme )
    {
        final VersionScheme versionScheme = VersionSchemeSelector.getTLVersionScheme( scheme );
        return createFromVersion( version, versionScheme );
    }

    public static SchemeAwareVersionRange createFromVersion( final String version, final VersionScheme scheme )
    {
        final List<Restriction> restrictions = Collections.emptyList();
        return new SchemeAwareVersionRange( new SchemeAwareArtifactVersion( version, scheme ), restrictions, scheme );
    }

    /**
     * Creates and returns a new <code>VersionRange</code> that is a restriction of this
     * version range and the specified version range.
     * <p>
     * Note: Precedence is given to the recommended version from this version range over the
     * recommended version from the specified version range.
     * </p>
     *
     * @param restriction the <code>VersionRange</code> that will be used to restrict this version
     *                    range.
     * @return the <code>VersionRange</code> that is a restriction of this version range and the
     *         specified version range.
     *         <p>
     *         The restrictions of the returned version range will be an intersection of the restrictions
     *         of this version range and the specified version range if both version ranges have
     *         restrictions. Otherwise, the restrictions on the returned range will be empty.
     *         </p>
     *         <p>
     *         The recommended version of the returned version range will be the recommended version of
     *         this version range, provided that ranges falls within the intersected restrictions. If
     *         the restrictions are empty, this version range's recommended version is used if it is not
     *         <code>null</code>. If it is <code>null</code>, the specified version range's recommended
     *         version is used (provided it is non-<code>null</code>). If no recommended version can be
     *         obtained, the returned version range's recommended version is set to <code>null</code>.
     *         </p>
     * @throws NullPointerException if the specified <code>VersionRange</code> is
     *                              <code>null</code>.
     */
    public SchemeAwareVersionRange restrict( final SchemeAwareVersionRange restriction )
    {
        final List<Restriction> r1 = getRestrictions();
        final List<Restriction> r2 = restriction.getRestrictions();
        List<Restriction> restrictions;

        if ( r1.isEmpty() || r2.isEmpty() )
        {
            restrictions = Collections.emptyList();
        }
        else
        {
            restrictions = intersection( r1, r2 );
        }

        ArtifactVersion version = null;
        if ( restrictions.size() > 0 )
        {
            for ( final Restriction r : restrictions )
            {
                if ( getRecommendedVersion() != null && r.containsVersion( getRecommendedVersion() ) )
                {
                    // if we find the original, use that
                    version = getRecommendedVersion();
                    break;
                }
                else if ( version == null && restriction.getRecommendedVersion() != null
                    && r.containsVersion( restriction.getRecommendedVersion() ) )
                {
                    // use this if we can, but prefer the original if possible
                    version = restriction.getRecommendedVersion();
                }
            }
        }
        // Either the original or the specified version ranges have no restrictions
        else if ( getRecommendedVersion() != null )
        {
            // Use the original recommended version since it exists
            version = getRecommendedVersion();
        }
        else if ( restriction.getRecommendedVersion() != null )
        {
            // Use the recommended version from the specified VersionRange since there is no
            // original recommended version
            version = restriction.getRecommendedVersion();
        }
        /* TODO: should throw this immediately, but need artifact
                else
                {
                    throw new OverConstrainedVersionException( "Restricting incompatible version ranges" );
                }
        */

        return new SchemeAwareVersionRange( version, restrictions, restriction.getVersionScheme() );
    }

    @SuppressWarnings( "unchecked" )
    private List<Restriction> intersection( final List<Restriction> r1, final List<Restriction> r2 )
    {
        final List<Restriction> restrictions = new ArrayList<Restriction>( r1.size() + r2.size() );
        final Iterator<Restriction> i1 = r1.iterator();
        final Iterator<Restriction> i2 = r2.iterator();
        Restriction res1 = i1.next();
        Restriction res2 = i2.next();

        boolean done = false;
        while ( !done )
        {
            if ( res1.getLowerBound() == null || res2.getUpperBound() == null
                || res1.getLowerBound().compareTo( res2.getUpperBound() ) <= 0 )
            {
                if ( res1.getUpperBound() == null || res2.getLowerBound() == null
                    || res1.getUpperBound().compareTo( res2.getLowerBound() ) >= 0 )
                {
                    ArtifactVersion lower;
                    ArtifactVersion upper;
                    boolean lowerInclusive;
                    boolean upperInclusive;

                    // overlaps
                    if ( res1.getLowerBound() == null )
                    {
                        lower = res2.getLowerBound();
                        lowerInclusive = res2.isLowerBoundInclusive();
                    }
                    else if ( res2.getLowerBound() == null )
                    {
                        lower = res1.getLowerBound();
                        lowerInclusive = res1.isLowerBoundInclusive();
                    }
                    else
                    {
                        final int comparison = res1.getLowerBound().compareTo( res2.getLowerBound() );
                        if ( comparison < 0 )
                        {
                            lower = res2.getLowerBound();
                            lowerInclusive = res2.isLowerBoundInclusive();
                        }
                        else if ( comparison == 0 )
                        {
                            lower = res1.getLowerBound();
                            lowerInclusive = res1.isLowerBoundInclusive() && res2.isLowerBoundInclusive();
                        }
                        else
                        {
                            lower = res1.getLowerBound();
                            lowerInclusive = res1.isLowerBoundInclusive();
                        }
                    }

                    if ( res1.getUpperBound() == null )
                    {
                        upper = res2.getUpperBound();
                        upperInclusive = res2.isUpperBoundInclusive();
                    }
                    else if ( res2.getUpperBound() == null )
                    {
                        upper = res1.getUpperBound();
                        upperInclusive = res1.isUpperBoundInclusive();
                    }
                    else
                    {
                        final int comparison = res1.getUpperBound().compareTo( res2.getUpperBound() );
                        if ( comparison < 0 )
                        {
                            upper = res1.getUpperBound();
                            upperInclusive = res1.isUpperBoundInclusive();
                        }
                        else if ( comparison == 0 )
                        {
                            upper = res1.getUpperBound();
                            upperInclusive = res1.isUpperBoundInclusive() && res2.isUpperBoundInclusive();
                        }
                        else
                        {
                            upper = res2.getUpperBound();
                            upperInclusive = res2.isUpperBoundInclusive();
                        }
                    }

                    // don't add if they are equal and one is not inclusive
                    if ( lower == null || upper == null || lower.compareTo( upper ) != 0 )
                    {
                        restrictions.add( new Restriction( lower, lowerInclusive, upper, upperInclusive ) );
                    }
                    else if ( lowerInclusive && upperInclusive )
                    {
                        restrictions.add( new Restriction( lower, lowerInclusive, upper, upperInclusive ) );
                    }

                    //noinspection ObjectEquality
                    if ( upper == res2.getUpperBound() )
                    {
                        // advance res2
                        if ( i2.hasNext() )
                        {
                            res2 = i2.next();
                        }
                        else
                        {
                            done = true;
                        }
                    }
                    else
                    {
                        // advance res1
                        if ( i1.hasNext() )
                        {
                            res1 = i1.next();
                        }
                        else
                        {
                            done = true;
                        }
                    }
                }
                else
                {
                    // move on to next in r1
                    if ( i1.hasNext() )
                    {
                        res1 = i1.next();
                    }
                    else
                    {
                        done = true;
                    }
                }
            }
            else
            {
                // move on to next in r2
                if ( i2.hasNext() )
                {
                    res2 = i2.next();
                }
                else
                {
                    done = true;
                }
            }
        }

        return restrictions;
    }

    public ArtifactVersion getSelectedVersion( final Artifact artifact )
        throws OverConstrainedVersionException
    {
        ArtifactVersion version;
        if ( getRecommendedVersion() != null )
        {
            version = getRecommendedVersion();
        }
        else
        {
            if ( getRestrictions().size() == 0 )
            {
                throw new OverConstrainedVersionException( "The artifact has no valid ranges", artifact );
            }

            version = null;
        }
        return version;
    }

    public boolean isSelectedVersionKnown( final Artifact artifact )
        throws OverConstrainedVersionException
    {
        boolean value = false;
        if ( getRecommendedVersion() != null )
        {
            value = true;
        }
        else
        {
            if ( getRestrictions().size() == 0 )
            {
                throw new OverConstrainedVersionException( "The artifact has no valid ranges", artifact );
            }
        }
        return value;
    }

    @Override
    public String toString()
    {
        if ( getRecommendedVersion() != null )
        {
            return getRecommendedVersion().toString();
        }
        else
        {
            final StringBuilder buf = new StringBuilder();
            for ( final Iterator<Restriction> i = getRestrictions().iterator(); i.hasNext(); )
            {
                final Restriction r = i.next();

                buf.append( r.toString() );

                if ( i.hasNext() )
                {
                    buf.append( ',' );
                }
            }
            return buf.toString();
        }
    }

    @SuppressWarnings( "unchecked" )
    public ArtifactVersion matchVersion( final List<ArtifactVersion> versions )
    {
        // TODO: could be more efficient by sorting the list and then moving along the restrictions in order?

        ArtifactVersion matched = null;
        for ( final ArtifactVersion version : versions )
        {
            if ( containsVersion( version ) )
            {
                // valid - check if it is greater than the currently matched version
                if ( matched == null || version.compareTo( matched ) > 0 )
                {
                    matched = version;
                }
            }
        }
        return matched;
    }

    public boolean containsVersion( final ArtifactVersion version )
    {
        for ( final Restriction restriction : getRestrictions() )
        {
            if ( restriction.containsVersion( version ) )
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasRestrictions()
    {
        return !getRestrictions().isEmpty() && getRecommendedVersion() == null;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof SchemeAwareVersionRange ) )
        {
            return false;
        }
        final SchemeAwareVersionRange other = (SchemeAwareVersionRange) obj;

        final ArtifactVersion recommendedVersion = getRecommendedVersion();
        final List<Restriction> restrictions = getRestrictions();

        boolean equals =
            recommendedVersion == other.getRecommendedVersion()
                || ( ( recommendedVersion != null ) && recommendedVersion.equals( other.getRecommendedVersion() ) );
        equals &=
            restrictions == other.getRestrictions()
                || ( ( restrictions != null ) && restrictions.equals( other.getRestrictions() ) );
        return equals;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + ( getRecommendedVersion() == null ? 0 : getRecommendedVersion().hashCode() );
        hash = 31 * hash + ( getRestrictions() == null ? 0 : getRestrictions().hashCode() );
        return hash;
    }

    public VersionScheme getVersionScheme()
    {
        return scheme;
    }

    public static boolean isRange( final String version )
    {
        final List<String> parts = new ArrayList<String>( Arrays.asList( version.split( "[,\\]\\[\\)\\(]" ) ) );
        for ( final Iterator<String> it = parts.iterator(); it.hasNext(); )
        {
            if ( it.next().trim().length() < 1 )
            {
                it.remove();
            }
        }

        if ( !parts.isEmpty() && parts.get( 0 ).equals( version ) )
        {
            return false;
        }

        return true;
    }
}
