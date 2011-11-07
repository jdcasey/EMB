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

package org.commonjava.emb.event;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component( role = EMBEventManager.class )
public class EMBEventManager
    implements Startable
{

    @Requirement( role = EMBSyncEventListener.class )
    private List<EMBSyncEventListener> syncListeners;

    @Requirement( role = EMBAsyncEventListener.class )
    private List<EMBAsyncEventListener> asyncListeners;

    private Executor executor;

    public void fireEvent( final EMBEvent event )
    {
        if ( asyncListeners != null )
        {
            for ( final EMBAsyncEventListener listener : asyncListeners )
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
            for ( final EMBSyncEventListener listener : syncListeners )
            {
                if ( listener != null && listener.canHandle( event ) )
                {
                    listener.handle( event );
                }
            }
        }
    }

    public void fireAsynchronousEvent( final EMBEvent event )
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
