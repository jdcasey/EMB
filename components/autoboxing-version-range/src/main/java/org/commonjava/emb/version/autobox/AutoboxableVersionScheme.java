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
