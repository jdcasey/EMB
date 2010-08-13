/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.commonjava.xaven.internal.plexus.guice;

import org.codehaus.plexus.component.annotations.Component;
import org.commonjava.xaven.plexus.ComponentKey;
import org.commonjava.xaven.plexus.ComponentSelector;
import org.sonatype.guice.bean.binders.QualifiedTypeBinder;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.LoadedClass;
import org.sonatype.guice.bean.scanners.QualifiedTypeListener;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.PlexusBeanDescription;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.config.Strategies;
import org.sonatype.guice.plexus.scanners.PlexusTypeListener;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

import java.lang.annotation.Annotation;
import java.util.Set;

/*
 * Given:
 * 
 * - DefaultPluginManager.class, role=PluginManager.class, hint=<default>
 * - CustomPluginManager.class, role=PluginManager.class, hint="custom"
 * - ComponentSelector with:
 * 
 *     - PluginManager.class/<default> => "custom"
 * 
 * Register the following:
 * 
 * - PluginManager.class/<default>  =>  Alias :: CustomPluginManager.class/"custom_"]
 * - PluginManager.class/"custom"   =>  Alias :: CustomPluginManager.class/"custom_"]
 * - PluginManager.class/"custom_"  =>  CustomPluginManager.class
 * 
 * - PluginManager.class/"default_" =>  DefaultPluginManager.class
 */
public final class SelectingTypeBinder
    implements PlexusTypeListener
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Binder binder;

    private final QualifiedTypeListener qualifiedTypeBinder;

    private final ComponentSelector componentSelector;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public SelectingTypeBinder( final ComponentSelector componentSelector, final Binder binder )
    {
        this.componentSelector = componentSelector;
        this.binder = binder;

        qualifiedTypeBinder = new QualifiedTypeBinder( binder );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        if ( Component.class == qualifier.annotationType() )
        {
            hear( (Component) qualifier, new LoadedClass<Object>( qualifiedType ), source );
        }
        else
        {
            qualifiedTypeBinder.hear( qualifier, qualifiedType, source );
        }
    }

    public void hear( final Component component, final DeferredClass<?> clazz, final Object source )
    {
        final String strategy = component.instantiationStrategy();
        final Class<?> role = component.role();
        final String hint = Hints.canonicalHint( component.hint() );

        final Binder componentBinder = componentBinder( source, component.description() );

        // if this component has been overridden with something else, let the overriding component 
        // create an alias from the main hint. Use hint_ here instead.
        if ( componentSelector.hasOverride( role, hint ) )
        {
            final Key<?> rootKey = Roles.componentKey( component.role(), Hints.canonicalHint( component.hint() ) + "_" );
            bind( rootKey, clazz, componentBinder, strategy, role );
        }
        else
        {
            final Set<ComponentKey<?>> overriddenKeys = componentSelector.getKeysOverriddenBy( role, hint );

            // bind the normal component role+hint, with the exception that we're forcing the explicit
            // use of the default hint here to avoid Guice running into an infinite loop looking up 
            // for the component role without a hint...which could be assigned as an alias.
            Key<?> rootKey;
            if ( Hints.isDefaultHint( hint ) )
            {
                rootKey = Key.get( role, Names.named( Hints.DEFAULT_HINT ) );
            }
            else
            {
                rootKey = Roles.componentKey( component );
            }

            bind( rootKey, clazz, componentBinder, strategy, role );

            // If this component overrides some other component, make aliases of the overridden hints
            // that point to this component. Bind this component normally.
            if ( overriddenKeys != null && !overriddenKeys.isEmpty() )
            {
                for ( final ComponentKey<?> ckey : overriddenKeys )
                {
                    final Key<?> key = Roles.componentKey( ckey.getRoleClass(), ckey.getHint() );
                    bindAlias( key, rootKey, componentBinder );
                }
            }
        }
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void bindAlias( final Key key, final Key rootKey, final Binder componentBinder )
    {
        componentBinder.bind( key ).to( rootKey );
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    private void bind( final Key key, final DeferredClass clazz, final Binder componentBinder, final String strategy,
                       final Class role )
    {
        final ScopedBindingBuilder sbb;
        // special case when role is the implementation
        if ( role.getName().equals( clazz.getName() ) )
        {
            if ( key.getAnnotation() != null )
            {
                sbb = componentBinder.bind( key ).to( role );
            }
            else
            {
                sbb = componentBinder.bind( key );
            }
        }
        else if ( Strategies.LOAD_ON_START.equals( strategy ) || clazz instanceof LoadedClass<?> )
        {
            sbb = componentBinder.bind( key ).to( clazz.load() ); // no need to defer
        }
        else
        {
            sbb = componentBinder.bind( key ).toProvider( clazz.asProvider() );
        }

        if ( Strategies.LOAD_ON_START.equals( strategy ) )
        {
            sbb.asEagerSingleton();
        }
        else if ( !Strategies.PER_LOOKUP.equals( strategy ) )
        {
            sbb.in( Scopes.SINGLETON );
        }
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private Binder componentBinder( final Object source, final String description )
    {
        if ( null != description && description.length() > 0 )
        {
            return binder.withSource( new DefaultPlexusBeanDescription( source, description ) );
        }
        return binder.withSource( source );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static final class DefaultPlexusBeanDescription
        implements PlexusBeanDescription
    {
        private final Object source;

        private final String description;

        DefaultPlexusBeanDescription( final Object source, final String description )
        {
            this.source = source;
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }

        @Override
        public String toString()
        {
            return source.toString();
        }
    }
}
