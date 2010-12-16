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

import static org.commonjava.emb.version.autobox.AutoboxingParser.parseVersion;
import static org.commonjava.emb.version.autobox.qual.RedHatQualifiers.INSTANCE;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sonatype.aether.version.InvalidVersionSpecificationException;

import java.util.ArrayList;
import java.util.List;

public class AutoboxableVersionTest
{

    @Test
    public void checkVersionSorting()
        throws InvalidVersionSpecificationException
    {
        final List<AutoboxableVersion> versions = new ArrayList<AutoboxableVersion>();

        versions.add( parseVersion( "1.0.2", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0.1-redhat-1", INSTANCE.rebuildIndicator(),
                                    INSTANCE.order() ) );
        versions.add( parseVersion( "1.0.1", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-rc2", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0RC1", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-beta-2", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-beta-1", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-a2", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-alpha-1", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-m1", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-SNAPSHOT", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0-20101122.121415-1", INSTANCE.rebuildIndicator(),
                                    INSTANCE.order() ) );

        for ( int i = 1; i < versions.size(); i++ )
        {
            final AutoboxableVersion later = versions.get( i - 1 );
            final AutoboxableVersion earlier = versions.get( i );

            assertTrue( later + " should sort AFTER " + earlier, later.compareTo( earlier ) > 0 );
        }
    }

}
