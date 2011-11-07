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

import static org.junit.Assert.assertEquals;

import org.commonjava.emb.event.testutils.TestEvent;
import org.junit.Test;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EMBEventTest
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
