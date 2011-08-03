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

import static org.commonjava.emb.version.autobox.AutoboxingParser.parseVersion;
import static org.commonjava.emb.version.autobox.qual.RedHatQualifiers.INSTANCE;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sonatype.aether.version.InvalidVersionSpecificationException;

public class AutoboxableVersionTest
{

    @Test
    public void checkVersionSorting()
        throws InvalidVersionSpecificationException
    {
        final List<AutoboxableVersion> versions = new ArrayList<AutoboxableVersion>();

        versions.add( parseVersion( "1.0.2", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
        versions.add( parseVersion( "1.0.1-redhat-1", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );
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
        versions.add( parseVersion( "1.0-20101122.121415-1", INSTANCE.rebuildIndicator(), INSTANCE.order() ) );

        for ( int i = 1; i < versions.size(); i++ )
        {
            final AutoboxableVersion later = versions.get( i - 1 );
            final AutoboxableVersion earlier = versions.get( i );

            assertTrue( later + " should sort AFTER " + earlier, later.compareTo( earlier ) > 0 );
        }
    }

}
