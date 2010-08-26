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

package org.commonjava.emb.resolve.event;

import com.google.inject.Inject;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventDispatcher<E extends Event>
{

    private final ExecutorService executor = Executors.newFixedThreadPool( 2 );

    @Inject
    private Set<EventHandler<E>> handlers;

    public void dispatch( final E event, final EventStatus status )
    {
        if ( handlers != null )
        {
            for ( final EventHandler<E> handler : handlers )
            {
                executor.execute( new Runnable()
                {
                    public void run()
                    {
                        handler.handle( event, status );
                    }
                } );
            }
        }
    }

}
