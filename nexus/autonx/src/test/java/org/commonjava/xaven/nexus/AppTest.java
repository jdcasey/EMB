package org.commonjava.xaven.nexus;

import org.junit.Before;
import org.junit.Test;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class AppTest
{

    private InetAddress[] addresses;

    private DirContext context;

    @Before
    public void setup()
        throws NamingException, UnknownHostException
    {
        final Map<String, String> env = new HashMap<String, String>();
        env.put( "java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory" );
        context = new InitialDirContext( new Hashtable<String, String>( env ) );

        final InetAddress lh = InetAddress.getLocalHost();
        addresses = InetAddress.getAllByName( lh.getHostName() );
    }

    //    @Test
    //    public void testSRVRecordList()
    //        throws NamingException
    //    {
    //        for ( final InetAddress addr : addresses )
    //        {
    //            final String hostname = addr.getCanonicalHostName();
    //            System.out.println( hostname + ": " + addr.getHostAddress() );
    //
    //            final int idx = hostname.indexOf( '.' );
    //            if ( idx > -1 )
    //            {
    //                final String domain = hostname.substring( idx + 1 );
    //
    //                final NamingEnumeration<NameClassPair> list = context.list( "_nexus." + domain );
    //                while ( list.hasMoreElements() )
    //                {
    //                    final NameClassPair pair = list.nextElement();
    //                    final Attributes attrs = context.getAttributes( pair.getNameInNamespace(), new String[] { "SRV" } );
    //                    System.out.println( attrs.get( "SRV" ).get() );
    //                }
    //            }
    //        }
    //    }
    //
    @Test
    public void testSRVRecordLookup()
        throws UnknownHostException, NamingException
    {

        String nexusHost = null;
        int nexusPort = -1;
        for ( final InetAddress addr : addresses )
        {
            final String hostname = addr.getCanonicalHostName();

            final int idx = hostname.indexOf( '.' );
            if ( idx > -1 )
            {
                final String domain = hostname.substring( idx + 1 );
                String record;
                try
                {
                    final Attributes attrs = context.getAttributes( "_nexus." + domain, new String[] { "SRV" } );
                    record = (String) attrs.get( "SRV" ).get();
                }
                catch ( final NamingException e )
                {
                    continue;
                }

                System.out.println( "SRV record: '" + record + "'" );
                final String[] parts = record.split( " " );

                nexusPort = Integer.parseInt( parts[parts.length - 2] );
                nexusHost = parts[parts.length - 1];
                if ( nexusHost.length() > 0 && nexusHost.charAt( nexusHost.length() - 1 ) == '.' )
                {
                    nexusHost = nexusHost.substring( 0, nexusHost.length() - 1 );
                }

                break;
            }
        }

        System.out.println( "Host: " + nexusHost + "\nPort: " + nexusPort );
    }

    @Test
    public void testTXTRecordLookup()
        throws UnknownHostException, NamingException
    {

        String nexusUrl = null;
        for ( final InetAddress addr : addresses )
        {
            final String hostname = addr.getCanonicalHostName();

            final int idx = hostname.indexOf( '.' );
            if ( idx > -1 )
            {
                final String domain = hostname.substring( idx + 1 );
                String record;
                try
                {
                    final Attributes attrs = context.getAttributes( "nxurl." + domain, new String[] { "TXT" } );
                    record = (String) attrs.get( "TXT" ).get();
                }
                catch ( final NamingException e )
                {
                    continue;
                }

                System.out.println( "TXT record: '" + record + "'" );
                final String[] parts = record.split( " " );

                nexusUrl = parts[parts.length - 1];

                break;
            }
        }

        System.out.println( "Nexus URL: " + nexusUrl );
    }

    @Test
    public void testSRV_TXT_ComboLookup()
        throws UnknownHostException, NamingException
    {

        String nexusHost = null;
        int nexusPort = -1;
        String nexusUrl = null;
        for ( final InetAddress addr : addresses )
        {
            final String hostname = addr.getCanonicalHostName();

            final int idx = hostname.indexOf( '.' );
            if ( idx > -1 )
            {
                final String domain = hostname.substring( idx + 1 );
                final Attributes attrs;
                try
                {
                    attrs = context.getAttributes( "_nexus." + domain, new String[] { "SRV", "TXT" } );
                }
                catch ( final NamingException e )
                {
                    continue;
                }

                String txtRecord = null;
                try
                {
                    txtRecord = (String) attrs.get( "TXT" ).get();
                }
                catch ( final NamingException e )
                {
                }

                if ( txtRecord != null )
                {
                    nexusUrl = txtRecord;
                    break;
                }

                String srvRecord = null;
                try
                {
                    srvRecord = (String) attrs.get( "SRV" ).get();
                }
                catch ( final NamingException e )
                {
                }

                if ( srvRecord != null )
                {
                    final String[] parts = srvRecord.split( " " );

                    nexusPort = Integer.parseInt( parts[parts.length - 2] );
                    nexusHost = parts[parts.length - 1];

                    if ( nexusHost.length() > 0 && nexusHost.charAt( nexusHost.length() - 1 ) == '.' )
                    {
                        nexusHost = nexusHost.substring( 0, nexusHost.length() - 1 );
                    }

                    break;
                }
            }
        }

        System.out.println( "Nexus URL: " + nexusUrl + "\nHost: " + nexusHost + "\nPort: " + nexusPort );
    }
}
