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

package org.commonjava.xaven.event.stack;

import org.commonjava.xaven.event.XavenEvent;

import java.util.Iterator;
import java.util.LinkedList;

public final class XavenEventStackImpl
    implements XavenPrivateEventStack
{

    private final LinkedList<XavenEvent> events = new LinkedList<XavenEvent>();

    private PublicWrapper wrapper;

    public synchronized int push( final XavenEvent event )
    {
        events.addFirst( event );
        return events.size();
    }

    public synchronized XavenEvent pop()
    {
        return events.removeFirst();
    }

    public synchronized XavenEvent peek()
    {
        return events.getFirst();
    }

    public boolean isEmpty()
    {
        return events.isEmpty();
    }

    public synchronized XavenEvent eventAt( final int depth )
    {
        if ( events.size() < depth )
        {
            return null;
        }

        return events.get( events.size() - depth );
    }

    public int search( final XavenEvent event )
    {
        return events.indexOf( event );
    }

    public synchronized int search( final Class<? extends XavenEvent> eventClass )
    {
        int idx = 0;
        for ( final XavenEvent event : events )
        {
            if ( eventClass.isAssignableFrom( event.getClass() ) )
            {
                return idx;
            }

            idx++;
        }

        return -1;
    }

    public Iterator<XavenEvent> iterator()
    {
        return new LinkedList<XavenEvent>( events ).iterator();
    }

    public synchronized XavenEventStack publicStack()
    {
        if ( wrapper == null )
        {
            wrapper = new PublicWrapper( this );
        }

        return wrapper;
    }

    public static final class PublicWrapper
        implements XavenEventStack
    {

        private final XavenPrivateEventStack impl;

        private PublicWrapper( final XavenPrivateEventStack impl )
        {
            this.impl = impl;
        }

        public boolean isEmpty()
        {
            return impl.isEmpty();
        }

        public XavenEvent eventAt( final int depth )
        {
            return impl.eventAt( depth );
        }

        public int search( final XavenEvent event )
        {
            return impl.search( event );
        }

        public int search( final Class<? extends XavenEvent> eventClass )
        {
            return impl.search( eventClass );
        }

        public XavenEvent peek()
        {
            return impl.peek();
        }

        public Iterator<XavenEvent> iterator()
        {
            final Iterator<XavenEvent> iter = impl.iterator();
            return new Iterator<XavenEvent>()
            {

                @Override
                public boolean hasNext()
                {
                    return iter.hasNext();
                }

                @Override
                public XavenEvent next()
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
