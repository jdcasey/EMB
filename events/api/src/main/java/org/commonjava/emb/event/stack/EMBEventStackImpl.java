/*
 * Copyright 2011 Red Hat, Inc.
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

package org.commonjava.emb.event.stack;

import org.commonjava.emb.event.EMBEvent;

import java.util.Iterator;
import java.util.LinkedList;

public final class EMBEventStackImpl
    implements EMBPrivateEventStack
{

    private final LinkedList<EMBEvent> events = new LinkedList<EMBEvent>();

    private PublicWrapper wrapper;

    public synchronized int push( final EMBEvent event )
    {
        events.addFirst( event );
        return events.size();
    }

    public synchronized EMBEvent pop()
    {
        return events.removeFirst();
    }

    public synchronized EMBEvent peek()
    {
        return events.getFirst();
    }

    public boolean isEmpty()
    {
        return events.isEmpty();
    }

    public synchronized EMBEvent eventAt( final int depth )
    {
        if ( events.size() < depth )
        {
            return null;
        }

        return events.get( events.size() - depth );
    }

    public int search( final EMBEvent event )
    {
        return events.indexOf( event );
    }

    public synchronized int search( final Class<? extends EMBEvent> eventClass )
    {
        int idx = 0;
        for ( final EMBEvent event : events )
        {
            if ( eventClass.isAssignableFrom( event.getClass() ) )
            {
                return idx;
            }

            idx++;
        }

        return -1;
    }

    public Iterator<EMBEvent> iterator()
    {
        return new LinkedList<EMBEvent>( events ).iterator();
    }

    public synchronized EMBEventStack publicStack()
    {
        if ( wrapper == null )
        {
            wrapper = new PublicWrapper( this );
        }

        return wrapper;
    }

    public static final class PublicWrapper
        implements EMBEventStack
    {

        private final EMBPrivateEventStack impl;

        private PublicWrapper( final EMBPrivateEventStack impl )
        {
            this.impl = impl;
        }

        public boolean isEmpty()
        {
            return impl.isEmpty();
        }

        public EMBEvent eventAt( final int depth )
        {
            return impl.eventAt( depth );
        }

        public int search( final EMBEvent event )
        {
            return impl.search( event );
        }

        public int search( final Class<? extends EMBEvent> eventClass )
        {
            return impl.search( eventClass );
        }

        public EMBEvent peek()
        {
            return impl.peek();
        }

        public Iterator<EMBEvent> iterator()
        {
            final Iterator<EMBEvent> iter = impl.iterator();
            return new Iterator<EMBEvent>()
            {

                @Override
                public boolean hasNext()
                {
                    return iter.hasNext();
                }

                @Override
                public EMBEvent next()
                {
                    return iter.next();
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException( "Not Implemented." );
                }
            };
        }

    }

}
