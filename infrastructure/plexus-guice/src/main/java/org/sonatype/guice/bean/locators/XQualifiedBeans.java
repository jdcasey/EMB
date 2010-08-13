/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.bean.locators;

import org.commonjava.xaven.plexus.ComponentKey;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * {@link Iterable} sequence of qualified beans backed by bindings from one or more {@link Injector}s.
 */
class XQualifiedBeans<Q extends Annotation, T>
    implements Iterable<QualifiedBean<Q, T>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final Annotation DEFAULT_QUALIFIER = Names.named( "default" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final QualifyingStrategy strategy;

    private final Key<T> key;

    private ArrayList<QualifiedBean<Q, T>> beans = null;

    private boolean exposed;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    XQualifiedBeans( final Key<T> key )
    {
        this.strategy = selectQualifyingStrategy( key );
        this.key = key;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public final synchronized Iterator<QualifiedBean<Q, T>> iterator()
    {
        if ( null != beans )
        {
            exposed = true;
            return beans.iterator();
        }
        return Collections.EMPTY_LIST.iterator();
    }

    /**
     * Adds qualified beans from the given injector to the current sequence.
     * 
     * @param injector The new injector
     * @return Added beans
     */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public synchronized List<QualifiedBean<Q, T>> add( final Injector injector )
    {
        final Collection<Binding<?>> bindings;
        final TypeLiteral bindingType = key.getTypeLiteral();
        Binding<?> b = null;
        if ( strategy == QualifyingStrategy.NAMED_WITH_ATTRIBUTES )
        {
            bindings = new HashSet<Binding<?>>();
            final Annotation annotation = key.getAnnotation();
            String hint = null;

            if ( annotation != null && ( annotation instanceof Named )
                && ComponentKey.isLiteral( ( (Named) annotation ).value() ) )
            {
                hint = ComponentKey.getLiteralHint( ( (Named) annotation ).value() );
                b = injector.getBindings().get( Key.get( bindingType, Names.named( hint ) ) );

                if ( null != b )
                {
                    bindings.add( b );
                }
            }
            else
            {
                b = injector.getBindings().get( key );
                if ( null != b )
                {
                    bindings.add( b );
                }
            }

            if ( DEFAULT_QUALIFIER.equals( annotation ) || ComponentKey.DEFAULT_HINT.equals( hint ) )
            {
                b = injector.getBindings().get( Key.get( bindingType ) );
                if ( null != b )
                {
                    bindings.add( b );
                }
            }
        }
        else
        {
            bindings = injector.findBindingsByType( bindingType );
        }

        if ( bindings.isEmpty() )
        {
            return Collections.EMPTY_LIST;
        }
        final List<QualifiedBean<Q, T>> newBeans = new ArrayList<QualifiedBean<Q, T>>();
        for ( final Binding binding : bindings )
        {
            if ( false == binding.getSource() instanceof HiddenSource )
            {
                final Q qualifier = (Q) strategy.qualify( key, binding );
                if ( null != qualifier )
                {
                    newBeans.add( new QualifiedBean<Q, T>( qualifier, binding ) );
                }
            }
        }
        if ( !newBeans.isEmpty() )
        {
            mergeQualifiedBeans( newBeans );
        }
        return newBeans;
    }

    /**
     * Removes qualified beans from the given injector from the current sequence.
     * 
     * @param injector The old injector
     * @return Removed beans
     */
    public synchronized List<QualifiedBean<Q, T>> remove( final Injector injector )
    {
        if ( null == beans )
        {
            return Collections.emptyList();
        }
        // use binding membership to identify which beans belong to removed injector
        final List<QualifiedBean<Q, T>> oldBeans = new ArrayList<QualifiedBean<Q, T>>();
        final Collection<Binding<?>> bindings = injector.getBindings().values();
        for ( int i = 0; i < beans.size(); i++ )
        {
            if ( bindings.contains( beans.get( i ).getBinding() ) )
            {
                if ( exposed )
                {
                    // take defensive copy to avoid disturbing iterators
                    beans = new ArrayList<QualifiedBean<Q, T>>( beans );
                    exposed = false;
                }
                oldBeans.add( beans.remove( i-- ) );
            }
        }
        if ( beans.isEmpty() )
        {
            beans = null;
        }
        else if ( !oldBeans.isEmpty() )
        {
            beans.trimToSize();
        }
        return oldBeans;
    }

    /**
     * Clears all qualified beans from the current sequence.
     * 
     * @return Cleared beans
     */
    public synchronized List<QualifiedBean<Q, T>> clear()
    {
        if ( null != beans )
        {
            final List<QualifiedBean<Q, T>> oldBeans = beans;
            beans = null;
            exposed = false;
            return oldBeans;
        }
        return Collections.emptyList();
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private static final QualifyingStrategy selectQualifyingStrategy( final Key<?> key )
    {
        final Class<?> qualifierType = key.getAnnotationType();
        if ( null == qualifierType )
        {
            return QualifyingStrategy.UNRESTRICTED;
        }
        if ( Named.class == qualifierType )
        {
            return key.hasAttributes() ? QualifyingStrategy.NAMED_WITH_ATTRIBUTES : QualifyingStrategy.NAMED;
        }
        return key.hasAttributes() ? QualifyingStrategy.MARKED_WITH_ATTRIBUTES : QualifyingStrategy.MARKED;
    }

    private void mergeQualifiedBeans( final List<QualifiedBean<Q, T>> newBeans )
    {
        final int numBeans = newBeans.size();
        if ( null == beans )
        {
            beans = new ArrayList<QualifiedBean<Q, T>>( numBeans );
        }
        else
        {
            beans.ensureCapacity( beans.size() + numBeans );
        }
        int defaultIndex = 0;
        while ( defaultIndex < beans.size() && DEFAULT_QUALIFIER.equals( beans.get( defaultIndex ).getKey() ) )
        {
            defaultIndex++;
        }
        for ( int i = 0; i < numBeans; i++ )
        {
            final QualifiedBean<Q, T> bean = newBeans.get( i );
            if ( DEFAULT_QUALIFIER.equals( bean.getKey() ) )
            {
                beans.add( defaultIndex++, bean );
            }
            else
            {
                beans.add( bean );
            }
        }
    }
}
