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
import org.commonjava.emb.plexus.VirtualInstance;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import java.util.Map;

public class InstanceBindingModule
    extends AbstractModule
{

    private final InstanceRegistry registry;

    public InstanceBindingModule( final InstanceRegistry registry )
    {
        this.registry = registry;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    protected void configure()
    {
        for ( final Map.Entry<ComponentKey<?>, Object> mapping : registry.getInstances().entrySet() )
        {
            final ComponentKey<?> key = mapping.getKey();
            final Object instance = mapping.getValue();

            if ( instance instanceof VirtualInstance )
            {
                bind( Roles.componentKey( key.getRoleClass(), key.getHint() ) ).toProvider( (Provider) instance );
            }
            else
            {
                bind( Roles.componentKey( key.getRoleClass(), key.getHint() ) ).toProvider( new InstanceProvider(
                                                                                                                  instance ) );
            }
        }
    }

    private static final class InstanceProvider<T>
        implements Provider<T>
    {
        @Inject
        private Injector injector;

        private final T instance;

        InstanceProvider( final T instance )
        {
            this.instance = instance;
        }

        @Override
        public T get()
        {
            injector.injectMembers( instance );
            return instance;
        }
    }

}
