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

import org.commonjava.emb.version.autobox.lib.AutoboxingConfig;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;
import org.sonatype.aether.version.VersionRange;
import org.sonatype.aether.version.VersionScheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoboxableVersionScheme
    implements VersionScheme
{

    public static final String MAX_REBUILD_NUMBER = Integer.toString( 99999 );

    private final List<String> qualifiers;

    private final String rebuildIndicator;

    private final boolean autoBox;

    public AutoboxableVersionScheme( final boolean autoBox, final String rebuildIndicator, final String... qualifiers )
    {
        this.autoBox = autoBox;
        this.rebuildIndicator = rebuildIndicator;
        this.qualifiers = new ArrayList<String>( Arrays.asList( qualifiers ) );
    }

    public AutoboxableVersionScheme( final AutoboxingConfig config )
    {
        autoBox = config.isAutoBox();
        rebuildIndicator = config.getRebuildQualifier();
        qualifiers = new ArrayList<String>( Arrays.asList( config.getQualifierOrder() ) );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.version.VersionScheme#parseVersion(java.lang.String)
     */
    @Override
    public Version parseVersion( final String version )
        throws InvalidVersionSpecificationException
    {
        return AutoboxingParser.parseVersion( version, rebuildIndicator, qualifiers );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.version.VersionScheme#parseVersionRange(java.lang.String)
     */
    @Override
    public VersionRange parseVersionRange( final String range )
        throws InvalidVersionSpecificationException
    {
        return AutoboxingParser.parseRange( range, rebuildIndicator, qualifiers, autoBox );
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.sonatype.aether.version.VersionScheme#parseVersionConstraint(java.lang.String)
     */
    @Override
    public VersionConstraint parseVersionConstraint( final String constraint )
        throws InvalidVersionSpecificationException
    {
        return AutoboxingParser.parseConstraint( constraint, rebuildIndicator, qualifiers, autoBox );
    }

}
