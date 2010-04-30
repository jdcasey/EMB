package org.commonjava.xaven.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class App
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

    public static void main( final String[] args )
    {
        try
        {
            final DatagramSocket socket = new DatagramSocket( PORT + 1 );
            final DatagramPacket ping = new DatagramPacket( new byte[] {}, 0, MULTICAST, PORT );

            socket.send( ping );

            final byte[] buf = new byte[2048];

            final DatagramPacket pong = new DatagramPacket( buf, buf.length );
            socket.receive( pong );

            final ByteArrayInputStream bain = new ByteArrayInputStream( buf );
            final Properties props = new Properties();
            props.load( bain );

            props.list( System.out );

            System.out.println( pong.getAddress().getCanonicalHostName() );

            final StringBuilder url = new StringBuilder();
            url.append( props.getProperty( "Protocol", "http" ) )
               .append( "://" )
               .append( pong.getAddress().getCanonicalHostName() )
               .append( ':' )
               .append( props.getProperty( "Port", "8081" ) )
               .append( props.getProperty( "Context-Path", "/nexus" ) )
               .append( "/service/local/autonx/mirrors" );

            System.out.println( "URL: " + url );

            socket.close();
        }
        catch ( final IOException e )
        {
            e.printStackTrace();
        }

    }
}
