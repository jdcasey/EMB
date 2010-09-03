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

package org.commonjava.emb.internal.plexus.guice;

import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.InstanceRegistry;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.AbstractModule;

import java.util.Map;

public class InstanceRegistryModule
    extends AbstractModule
{

    private final InstanceRegistry registry;

    public InstanceRegistryModule( final InstanceRegistry registry )
    {
        this.registry = registry;
    }

    @Override
    protected void configure()
    {
        for ( final Map.Entry<ComponentKey<?>, Object> mapping : registry.getInstances().entrySet() )
        {
            bind( mapping.getKey(), mapping.getValue() );
        }
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void bind( final ComponentKey key, final Object instance )
    {
        bind( Roles.componentKey( key.getRoleClass(), key.getHint() ) ).toInstance( instance );
    }

}
