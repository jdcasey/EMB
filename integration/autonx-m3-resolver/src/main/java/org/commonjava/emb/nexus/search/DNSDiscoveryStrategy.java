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

import org.codehaus.plexus.component.annotations.Component;
import org.commonjava.emb.nexus.AutoNXException;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;

@Component( role = NexusDiscoveryStrategy.class, hint = "dns" )
public class DNSDiscoveryStrategy
    implements NexusDiscoveryStrategy
{

    public LinkedHashSet<String> findNexusCandidates()
        throws AutoNXException
    {
        final LinkedHashSet<String> candidates = new LinkedHashSet<String>();

        final Map<String, String> env = new HashMap<String, String>();
        env.put( "java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory" );

        DirContext jndiContext;
        try
        {
            jndiContext = new InitialDirContext( new Hashtable<String, String>( env ) );
        }
        catch ( final NamingException e )
        {
            throw new AutoNXException( "[autonx] Failed to initialize JNDI context for DNS lookups: {0}", e,
                                       e.getMessage() );
        }

        InetAddress[] addresses;
        try
        {
            final InetAddress lh = InetAddress.getLocalHost();
            addresses = InetAddress.getAllByName( lh.getHostName() );
        }
        catch ( final UnknownHostException e )
        {
            throw new AutoNXException( "[autonx] Failed to retrieve local hostnames: {0}", e, e.getMessage() );
        }

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
                    attrs = jndiContext.getAttributes( "_nexus." + domain, new String[] { "TXT" } );
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
                    candidates.add( txtRecord );
                }
            }
        }

        return candidates;
    }

}
