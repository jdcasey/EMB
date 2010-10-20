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

import org.codehaus.plexus.component.annotations.Component;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;
import org.commonjava.emb.plexus.InstanceRegistry;
import org.commonjava.emb.plexus.VirtualInstance;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.config.Roles;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import java.util.Map;

public class InstanceBindingModule
    implements PlexusBeanModule
{

    private final InstanceRegistry registry;

    private final ComponentSelector selector;

    private final Map<?, ?> variables;

    public InstanceBindingModule( final InstanceRegistry registry, final ComponentSelector selector,
                                  final Map<?, ?> variables )
    {
        this.registry = registry;
        this.selector = selector;
        this.variables = variables;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    public PlexusBeanSource configure( final Binder binder )
    {
        final SelectingTypeBinder typeBinder = new SelectingTypeBinder( selector, registry, binder );
        for ( final Map.Entry<ComponentKey<?>, Object> mapping : registry.getInstances().entrySet() )
        {
            final ComponentKey<?> key = mapping.getKey();
            final Object instance = mapping.getValue();

            if ( instance instanceof VirtualInstance )
            {
                final VirtualInstance vi = (VirtualInstance) instance;
                final Class<?> cls = vi.getVirtualClass();

                final Component comp = cls.getAnnotation( Component.class );
                if ( comp != null )
                {
                    typeBinder.hear( comp, new LoadedClass<Object>( cls ),
                                     "External instance loaded from: " + cls.getClassLoader(), vi );
                }
                else
                {
                    binder.bind( Roles.componentKey( key.getRoleClass(), key.getHint() ) )
                          .toProvider( (Provider) instance );
                }
            }
            else
            {
                final InstanceProvider provider = new InstanceProvider( instance );

                final Component comp = instance.getClass().getAnnotation( Component.class );
                if ( comp != null )
                {
                    typeBinder.hear( comp, new LoadedClass<Object>( instance.getClass() ),
                                     "External instance loaded from: " + instance.getClass().getClassLoader(), provider );
                }
                else
                {
                    binder.bind( Roles.componentKey( key.getRoleClass(), key.getHint() ) ).toProvider( provider );
                }
            }
        }

        return new XAnnotatedBeanSource( variables );
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
