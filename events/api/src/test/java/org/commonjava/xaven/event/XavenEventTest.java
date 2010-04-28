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

import static org.junit.Assert.assertEquals;

import org.commonjava.xaven.event.testutils.TestEvent;
import org.junit.Test;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class XavenEventTest
{

    @Test
    public void testChainIdUnchangedWhenDuringThreadInheritance()
    {
        final TestEvent topEvent = new TestEvent();
        final Long topEventId = topEvent.getEventChainId();

        final Executor executor = Executors.newFixedThreadPool( 3 );

        final int iterations = 20;
        final CountDownLatch latch = new CountDownLatch( iterations );
        final Vector<Long> eventChainIds = new Vector<Long>();
        for ( int i = 0; i < iterations; i++ )
        {
            executor.execute( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        final TestEvent event = new TestEvent();
                        System.out.println( "From thread: " + Thread.currentThread().getName() + "; eventChainId is: "
                            + event.getEventChainId() );
                        eventChainIds.add( event.getEventChainId() );
                    }
                    finally
                    {
                        latch.countDown();
                    }
                }
            } );
        }

        while ( latch.getCount() > 0 )
        {
            System.out.println( "Waiting for " + latch.getCount() + " threads to complete." );
            try
            {
                latch.await( 10, TimeUnit.MILLISECONDS );
            }
            catch ( final InterruptedException e )
            {
                System.out.println( "interrupted!" );
                break;
            }
        }

        assertEquals( topEventId.longValue(), topEvent.getEventChainId() );
        for ( final Long id : eventChainIds )
        {
            assertEquals( topEventId, id );
        }
    }

}
