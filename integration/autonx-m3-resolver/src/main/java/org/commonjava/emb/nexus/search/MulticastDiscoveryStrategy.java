/*
 * Copyright 2010 Red Hat, Inc.
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

package org.commonjava.emb.nexus.search;

import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.nexus.AutoNXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Properties;

// Disable this from the discovery pool for now, until I can straighten out the 
// usage/sysadmin implications of multicast...
//@Component( role = NexusDiscoveryStrategy.class, hint = "multicast" )
public class MulticastDiscoveryStrategy
    implements NexusDiscoveryStrategy
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

    @Requirement( hint = "autonx" )
    private EMBLibrary library;

    // @Inject
    // public MulticastDiscoveryStrategy( final EMBLibrary library )
    // {
    // this.library = library;
    // }

    public LinkedHashSet<String> findNexusCandidates()
        throws AutoNXException
    {
        final LinkedHashSet<String> urls = new LinkedHashSet<String>();

        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket( PORT + 1 );
            final DatagramPacket ping = new DatagramPacket( new byte[] {}, 0, MULTICAST, PORT );
            socket.setSoTimeout( 5000 );

            final Logger logger = library.getLogger();
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Sending UDP PING..." );
            }

            socket.send( ping );

            final byte[] buf = new byte[2048];

            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Waiting for PONG..." );
            }
            final DatagramPacket pong = new DatagramPacket( buf, buf.length );
            socket.receive( pong );

            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Received response from: " + pong.getAddress().getCanonicalHostName() );
            }

            final ByteArrayInputStream bain = new ByteArrayInputStream( buf );
            final Properties props = new Properties();
            props.load( bain );

            final StringBuilder url = new StringBuilder();
            url.append( props.getProperty( "Protocol", "http" ) )
               .append( "://" )
               .append( pong.getAddress().getCanonicalHostName() )
               .append( ':' )
               .append( props.getProperty( "Port", "8081" ) )
               .append( props.getProperty( "Context-Path", "/nexus" ) );

            urls.add( url.toString() );
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
        }
        finally
        {
            if ( socket != null )
            {
                socket.close();
            }
        }

        return urls;
    }

}
