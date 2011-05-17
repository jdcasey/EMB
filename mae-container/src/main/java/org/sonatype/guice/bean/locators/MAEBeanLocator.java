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

import org.sonatype.guice.bean.locators.BeanLocator;
import org.sonatype.guice.bean.locators.DefaultRankingFunction;
import org.sonatype.guice.bean.locators.InjectorPublisher;
import org.sonatype.guice.bean.locators.XLocatedBeans;
import org.sonatype.guice.bean.locators.MutableBeanLocator;
import org.sonatype.guice.bean.locators.XRankedBindings;
import org.sonatype.guice.bean.locators.RankedList;
import org.sonatype.guice.bean.locators.RankingFunction;
import org.sonatype.guice.bean.locators.spi.BindingDistributor;
import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.inject.BeanEntry;
import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Default {@link BeanLocator} that locates qualified beans across a dynamic group of {@link Injector}s.
 */
@Singleton
@SuppressWarnings( { "rawtypes", "unchecked" } )
public final class MAEBeanLocator
    implements MutableBeanLocator
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final RankedList<BindingPublisher> publishers = new RankedList<BindingPublisher>();

    private final Map<TypeLiteral, XRankedBindings> bindingsCache = new HashMap<TypeLiteral, XRankedBindings>();

    private final List<XWatchedBeans> watchedBeans = new ArrayList<XWatchedBeans>();
    
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------
    
    public synchronized Iterable<BeanEntry> locate( final Key key )
    {
        return new XLocatedBeans( key, bindingsForType( key.getTypeLiteral() ) );
    }

    public synchronized void watch( final Key key, final Mediator mediator, final Object watcher )
    {
        final XWatchedBeans beans = new XWatchedBeans( key, mediator, watcher );
        for ( int i = 0, size = publishers.size(); i < size; i++ )
        {
            beans.add( publishers.get( i ), 0 /* unused */);
        }
        watchedBeans.add( beans );
    }

    public void add( final Injector injector, final int rank )
    {
        add( new InjectorPublisher( injector, new DefaultRankingFunction( rank ) ), rank );
    }

    public void remove( final Injector injector )
    {
        remove( new InjectorPublisher( injector, null ) );
    }

    public synchronized void add( final BindingPublisher publisher, final int rank )
    {
        if ( !publishers.contains( publisher ) )
        {
            publishers.insert( publisher, rank );
            distribute( BindingEvent.ADD, publisher, rank );
        }
    }

    public synchronized void remove( final BindingPublisher publisher )
    {
        if ( publishers.remove( publisher ) )
        {
            distribute( BindingEvent.REMOVE, publisher, 0 );
        }
    }

    public synchronized void clear()
    {
        publishers.clear();
        distribute( BindingEvent.CLEAR, null, 0 );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Automatically publishes any {@link Injector} that contains a binding to this {@link BeanLocator}.
     * 
     * @param injector The injector
     */
    @Inject
    void autoPublish( final Injector injector )
    {
        final RankingFunction function = injector.getInstance( RankingFunction.class );
        add( new InjectorPublisher( injector, function ), function.maxRank() );
    }

    /**
     * Returns the {@link XRankedBindings} tracking the given type; creates one if it doesn't already exist.
     * 
     * @param type The required type
     * @return Sequence of ranked bindings
     */
    private <T> XRankedBindings<T> bindingsForType( final TypeLiteral<T> type )
    {
        XRankedBindings<T> bindings = bindingsCache.get( type );
        if ( null == bindings )
        {
            bindings = new XRankedBindings<T>( type, publishers );
            bindingsCache.put( type, bindings );
        }
        return bindings;
    }

    /**
     * Distributes the given binding event to interested parties.
     * 
     * @param event The binding event
     * @param publisher The optional publisher
     * @param rank The optional assigned rank
     */
    private void distribute( final BindingEvent event, final BindingPublisher publisher, final int rank )
    {
        for ( final Iterator<XRankedBindings> itr = bindingsCache.values().iterator(); itr.hasNext(); )
        {
            final XRankedBindings bindings = itr.next();
            if ( bindings.isActive() )
            {
                notify( bindings, event, publisher, rank );
            }
            else
            {
                itr.remove(); // cleanup up stale entries
            }
        }

        for ( int i = 0; i < watchedBeans.size(); i++ )
        {
            final XWatchedBeans beans = watchedBeans.get( i );
            if ( beans.isActive() )
            {
                notify( beans, event, publisher, rank );
            }
            else
            {
                watchedBeans.remove( i-- ); // cleanup up stale entries
            }
        }
    }

    /**
     * Notifies the given distributor of the given binding event and its optional details.
     * 
     * @param distributor The distributor
     * @param event The binding event
     * @param publisher The optional publisher
     * @param rank The optional assigned rank
     */
    private static void notify( final BindingDistributor distributor, final BindingEvent event,
                                final BindingPublisher publisher, final int rank )
    {
        switch ( event )
        {
            case ADD:
                distributor.add( publisher, rank );
                break;
            case REMOVE:
                distributor.remove( publisher );
                break;
            case CLEAR:
                distributor.clear();
                break;
        }
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private static enum BindingEvent
    {
        ADD, REMOVE, CLEAR
    }
}
