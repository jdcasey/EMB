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

package org.commonjava.xaven.event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component( role = XavenEventManager.class )
public class XavenEventManager
    implements Startable
{

    @Requirement( role = XavenSyncEventListener.class )
    private List<XavenSyncEventListener> syncListeners;

    @Requirement( role = XavenAsyncEventListener.class )
    private List<XavenAsyncEventListener> asyncListeners;

    private Executor executor;

    public void fireEvent( final XavenEvent event )
    {
        if ( asyncListeners != null )
        {
            for ( final XavenAsyncEventListener listener : asyncListeners )
            {
                if ( listener != null && listener.canHandle( event ) )
                {
                    executor.execute( new Runnable()
                    {
                        public void run()
                        {
                            listener.handle( event );
                        }
                    } );
                }
            }
        }

        if ( syncListeners != null )
        {
            for ( final XavenSyncEventListener listener : syncListeners )
            {
                if ( listener != null && listener.canHandle( event ) )
                {
                    listener.handle( event );
                }
            }
        }
    }

    public void fireAsynchronousEvent( final XavenEvent event )
    {
    }

    public void start()
        throws StartingException
    {
        executor = Executors.newCachedThreadPool();
    }

    public void stop()
        throws StoppingException
    {
    }

}
