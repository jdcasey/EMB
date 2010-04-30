package org.commonjava.xaven.nexus.search;

import org.codehaus.plexus.component.annotations.Component;
import org.commonjava.xaven.nexus.AutoNXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Properties;

@Component( role = NexusDiscoveryStrategy.class, hint = "multicast" )
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

    public LinkedHashSet<String> findNexusCandidates()
        throws AutoNXException
    {
        final LinkedHashSet<String> urls = new LinkedHashSet<String>();

        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket( PORT + 1 );
            final DatagramPacket ping = new DatagramPacket( new byte[] {}, 0, MULTICAST, PORT );

            socket.send( ping );

            final byte[] buf = new byte[2048];

            final DatagramPacket pong = new DatagramPacket( buf, buf.length );
            socket.receive( pong );

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
