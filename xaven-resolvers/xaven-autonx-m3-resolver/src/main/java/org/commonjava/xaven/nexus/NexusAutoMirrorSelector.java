package org.commonjava.xaven.nexus;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.xaven.nexus.conf.AutoNXConfiguration;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component( role = MirrorSelector.class, hint = "nexus-auto" )
public class NexusAutoMirrorSelector
    implements MirrorSelector, Initializable, LogEnabled
{

    private Map<String, Mirror> autodetectedMirrors;

    @Requirement
    private MirrorSelector delegateSelector;

    @Requirement
    private AutoNXConfiguration autonxConfig;

    private Logger logger;

    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        Mirror mirror = autodetectedMirrors.get( repository.getUrl() );
        if ( mirror == null )
        {
            mirror = delegateSelector.getMirror( repository, mirrors );
        }

        return mirror;
    }

    public void initialize()
        throws InitializationException
    {
        String url = null;
        if ( autonxConfig.getNexusUrl() != null )
        {
            url = autonxConfig.getNexusUrl();
        }
        else
        {
            url = findNexus();
        }
    }

    protected String findNexus()
        throws InitializationException
    {
        final Set<String> candidates = new LinkedHashSet<String>();
        // pile up candidates, then check them to (hopefully) find one that works.

        String result = null;

        final Map<String, String> env = new HashMap<String, String>();
        env.put( "java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory" );

        DirContext jndiContext;
        try
        {
            jndiContext = new InitialDirContext( new Hashtable<String, String>( env ) );
        }
        catch ( final NamingException e )
        {
            throw new InitializationException( "[autonx] Failed to initialize JNDI context for DNS lookups: "
                + e.getMessage(), e );
        }

        InetAddress[] addresses;
        try
        {
            final InetAddress lh = InetAddress.getLocalHost();
            addresses = InetAddress.getAllByName( lh.getHostName() );
        }
        catch ( final UnknownHostException e )
        {
            throw new InitializationException( "[autonx] Failed to retrieve local hostnames: " + e.getMessage(), e );
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
                    attrs = jndiContext.getAttributes( "_nexus." + domain, new String[] { "SRV", "TXT" } );
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
                    result = txtRecord;
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

                    final int nexusPort = Integer.parseInt( parts[parts.length - 2] );
                    String nexusHost = parts[parts.length - 1];

                    if ( nexusHost.length() > 0 && nexusHost.charAt( nexusHost.length() - 1 ) == '.' )
                    {
                        nexusHost = nexusHost.substring( 0, nexusHost.length() - 1 );
                    }

                    break;
                }
            }
        }

        return result;
    }

    protected final synchronized Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_WARN, "internal" );
        }

        return logger;
    }

    public final void enableLogging( final Logger logger )
    {
        this.logger = logger;
    }

}
