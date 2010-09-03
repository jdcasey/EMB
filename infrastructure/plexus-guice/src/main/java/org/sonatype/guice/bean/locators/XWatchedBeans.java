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

import org.sonatype.inject.Mediator;

import com.google.inject.Injector;
import com.google.inject.Key;

import javax.inject.Provider;

import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Iterable} sequence of qualified beans that uses a {@link Mediator} to send updates to watching objects.
 */
final class XWatchedBeans<Q extends Annotation, T, W>
    extends XQualifiedBeans<Q, T>
    implements Provider<XQualifiedBeans<Q, T>>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Reference<W> watcherRef;

    private final Mediator<Q, T, W> mediator;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    XWatchedBeans( final Key<T> key, final Mediator<Q, T, W> mediator, final W watcher )
    {
        super( key );

        // use weak-ref to detect when watcher disappears
        this.watcherRef = new WeakReference<W>( watcher );
        this.mediator = mediator;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public XQualifiedBeans<Q, T> get()
    {
        // bean sequence disappears when watcher does
        return null != watcherRef.get() ? this : null;
    }

    @Override
    public synchronized List<QualifiedBean<Q, T>> add( final Injector injector )
    {
        return reportBeans( BeanEvent.ADD, super.add( injector ) );
    }

    @Override
    public synchronized List<QualifiedBean<Q, T>> remove( final Injector injector )
    {
        return reportBeans( BeanEvent.REMOVE, super.remove( injector ) );
    }

    @Override
    public synchronized List<QualifiedBean<Q, T>> clear()
    {
        return reportBeans( BeanEvent.REMOVE, super.clear() );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    /**
     * Enumerate the various bean events that can be sent via the {@link Mediator}.
     */
    private static enum BeanEvent
    {
        ADD
        {
            @Override
            public <Q extends Annotation, T, W> void send( final Mediator<Q, T, W> mediator,
                                                           final QualifiedBean<Q, T> bean, final W watcher )
                throws Exception
            {
                mediator.add( bean.getKey(), bean, watcher );
            }
        },
        REMOVE
        {
            @Override
            public <Q extends Annotation, T, W> void send( final Mediator<Q, T, W> mediator,
                                                           final QualifiedBean<Q, T> bean, final W watcher )
                throws Exception
            {
                mediator.remove( bean.getKey(), bean, watcher );
            }
        };

        public abstract <Q extends Annotation, T, W> void send( final Mediator<Q, T, W> mediator,
                                                                QualifiedBean<Q, T> bean, W watcher )
            throws Exception;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Uses the given event to report a series of qualified beans to a watcher via the {@link Mediator}.
     * 
     * @param event The bean event
     * @param beans The qualified beans
     * @return The reported beans
     */
    private List<QualifiedBean<Q, T>> reportBeans( final BeanEvent event, final List<QualifiedBean<Q, T>> beans )
    {
        final W watcher = watcherRef.get();
        if ( null != watcher )
        {
            for ( int i = 0, size = beans.size(); i < size; i++ )
            {
                try
                {
                    event.send( mediator, beans.get( i ), watcher );
                }
                catch ( final Throwable e )
                {
                    reportWatcherException( watcher, e );
                }
            }
        }
        return beans;
    }

    /**
     * Reports the given watcher exception to the SLF4J logger if available; otherwise to JUL.
     * 
     * @param watcher The bean watcher
     * @param exception The exception
     */
    private void reportWatcherException( final Object watcher, final Throwable exception )
    {
        final String message = "Problem notifying watcher: " + watcher;
        final Class<?> mediatorType = mediator.getClass();
        try
        {
            org.slf4j.LoggerFactory.getLogger( mediatorType ).warn( message, exception );
        }
        catch ( final Throwable ignore )
        {
            Logger.getLogger( mediatorType.getName() ).log( Level.WARNING, message, exception );
        }
    }
}
