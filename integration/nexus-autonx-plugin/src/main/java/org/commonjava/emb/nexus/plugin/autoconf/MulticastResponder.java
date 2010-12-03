/*
 * Copyright (c) 2010 Red Hat, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see 
 * <http://www.gnu.org/licenses>.
 */

package org.commonjava.emb.nexus.plugin.autoconf;

import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@Named( "multicast" )
public class MulticastResponder
    implements NexusResponder, Startable, Contextualizable
{

    public static InetAddress MULTICAST;

    static
    {
        try
        {
            MULTICAST = InetAddress.getByAddress( new byte[] { (byte) 239, (byte) 77, (byte) 124, (byte) 213 } );
        }
        catch ( final UnknownHostException e )
        {
            throw new Error( e );
        }
    }

    public static final int PORT = Integer.getInteger( "nexus.autonx.udp", 33849 );

    private MulticastSocket socket;

    private Thread listenerThread;

    private Listener listener;

    private Context context;

    private static final Logger logger = LoggerFactory.getLogger( MulticastResponder.class );

    private static final int TIMEOUT = 1000;

    public void start()
        throws StartingException
    {
        logger.info( "Starting multicast responder on: " + MULTICAST.getHostAddress() + ":" + PORT );
        try
        {
            socket = new MulticastSocket( PORT );
            socket.setSoTimeout( TIMEOUT );
            socket.joinGroup( MULTICAST );
        }
        catch ( final SocketException e )
        {
            throw new StartingException( "Failed to listen on multicast port: " + PORT + "\nReason: " + e.getMessage(),
                                         e );
        }
        catch ( final IOException e )
        {
            throw new StartingException( "Failed to listen on multicast port: " + PORT + "\nReason: " + e.getMessage(),
                                         e );
        }

        listener = new Listener( socket, context );
        listenerThread = new Thread( listener );
        listenerThread.setDaemon( true );

        listenerThread.start();

        logger.info( "...multicast responder started." );
    }

    public void stop()
        throws StoppingException
    {
        logger.info( "Stopping multicast responder..." );

        listener.shutdown();
        listenerThread.interrupt();

        int tries = 0;
        while ( listenerThread.isAlive() && tries < 5 )
        {
            logger.info( "Waiting 5s for multicast responder thread to die..." );

            try
            {
                listenerThread.join( 5000 );
            }
            catch ( final InterruptedException e )
            {
                Thread.currentThread().interrupt();
                break;
            }

            tries++;
        }

        if ( listenerThread.isAlive() )
        {
            logger.info( "Multicast responder thread didn't complete. Proceeding anyway." );
        }

        if ( socket != null )
        {
            socket.close();
        }

        logger.info( "...multicast responder stopped." );
    }

    private static final class Listener
        implements Runnable
    {
        private final MulticastSocket socket;

        private final Context context;

        private boolean shutdown = false;

        private Listener( final MulticastSocket socket, final Context context )
        {
            this.socket = socket;
            this.context = context;
        }

        private void shutdown()
        {
            shutdown = true;
        }

        public void run()
        {
            Object port = null;
            Object contextPath = null;
            String protocol = null;
            try
            {
                contextPath = context.get( "webapp-context-path" );

                if ( context.contains( "application-port-ssl" ) )
                {
                    port = context.get( "application-port-ssl" );
                    protocol = "https";
                }
                else if ( context.contains( "application-port" ) )
                {
                    port = context.get( "application-port" );
                    protocol = "http";
                }

            }
            catch ( final ContextException e )
            {
            }

            if ( port == null )
            {
                port = "8081";
            }

            if ( protocol == null )
            {
                protocol = "http";
            }

            if ( contextPath == null )
            {
                contextPath = "/nexus";
            }

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try
            {
                baos.write( ( "Protocol: " + protocol + "\n" ).getBytes() );
                baos.write( ( "Port: " + port + "\n" ).getBytes() );
                baos.write( ( "Context-Path: " + contextPath + "\n" ).getBytes() );
            }
            catch ( final IOException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.error( "Failed to write packet content. Reason: " + e.getMessage(), e );
                }
                else
                {
                    logger.error( "Failed to write packet content. Reason: " + e.getMessage() );
                }
            }

            while ( !shutdown )
            {
                try
                {
                    final byte[] buf = new byte[1];
                    final DatagramPacket recv = new DatagramPacket( buf, buf.length );

                    socket.receive( recv );

                    //                    if ( logger.isDebugEnabled() )
                    //                    {
                    logger.info( "\n\n\n\nGot request from: " + recv.getSocketAddress() );
                    try
                    {
                        logger.info( new String( baos.toByteArray(), "UTF-8" ) );
                    }
                    catch ( final UnsupportedEncodingException e )
                    {
                        logger.info( new String( baos.toByteArray() ) );
                    }
                    logger.info( "\n\n\n\n" );
                    //                    }

                    final DatagramPacket send =
                        new DatagramPacket( baos.toByteArray(), baos.size(), recv.getSocketAddress() );

                    socket.send( send );
                }
                catch ( final SocketTimeoutException e )
                {
                }
                catch ( final IOException e )
                {
                    if ( logger.isDebugEnabled() )
                    {
                        logger.error( String.format( "Multicast conversation failed! Reason: %s", e.getMessage() ), e );
                    }
                    else
                    {
                        logger.error( String.format( "Multicast conversation failed! Reason: %s", e.getMessage() ) );
                    }
                }
            }
        }
    }

    public void contextualize( final Context context )
        throws ContextException
    {
        this.context = context;
    }

}
