/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonjava.emb.internal.plexus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.commonjava.emb.internal.plexus.fixture.Child;
import org.commonjava.emb.internal.plexus.fixture.MapOwner;
import org.commonjava.emb.internal.plexus.fixture.SingletonLiteralOwner;
import org.commonjava.emb.internal.plexus.fixture.SingletonOwner;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;
import org.commonjava.emb.plexus.InstanceRegistry;
import org.junit.Test;

import java.util.Map;

public class EMBPlexusContainerTest
{

    @Test
    public void mappedRequirementContainsNoLiteralIds()
        throws Throwable
    {
        final ContainerConfiguration config = new DefaultContainerConfiguration().setClassPathScanning( true );

        final EMBPlexusContainer container =
            new EMBPlexusContainer( config, new ComponentSelector(), new InstanceRegistry() );

        final MapOwner mapOwner = container.lookup( MapOwner.class );
        final Map<String, Child> members = mapOwner.members();

        System.out.println( members );

        assertNull( members.get( "simple" + ComponentKey.LITERAL_SUFFIX ) );
    }

    @Test
    public void singletonNonLiteralRequirement()
        throws Throwable
    {
        final ContainerConfiguration config = new DefaultContainerConfiguration().setClassPathScanning( true );

        final EMBPlexusContainer container =
            new EMBPlexusContainer( config, new ComponentSelector(), new InstanceRegistry() );

        final SingletonOwner owner = container.lookup( SingletonOwner.class );

        assertNotNull( owner.singleton() );
    }

    @Test
    public void singletonLiteralRequirement()
        throws Throwable
    {
        final ContainerConfiguration config = new DefaultContainerConfiguration().setClassPathScanning( true );

        final EMBPlexusContainer container =
            new EMBPlexusContainer( config, new ComponentSelector(), new InstanceRegistry() );

        final SingletonLiteralOwner owner = container.lookup( SingletonLiteralOwner.class );

        assertNotNull( owner.singletonLiteral() );
    }

}
