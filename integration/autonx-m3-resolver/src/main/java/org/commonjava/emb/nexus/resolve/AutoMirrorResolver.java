package org.commonjava.emb.nexus.resolve;

import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.nexus.conf.AutoNXConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

@Component( role = AutoMirrorResolver.class )
public class AutoMirrorResolver
    implements Initializable
{

    private boolean initialized = false;

    @Requirement( hint = "autonx" )
    private EMBLibrary library;

    @Requirement
    private AutoNXConfiguration autonxConfig;

    @Requirement
    private EMBConfiguration embConfig;

    @Requirement( hint = "emb" )
    private Prompter prompter;

    private HttpClient client;

    // @Inject
    // public AutoMirrorResolver( @Named( "autonx" ) final EMBLibrary library, final AutoNXConfiguration autonxConfig,
    // final EMBConfiguration embConfig, @Named( "emb" ) final Prompter prompter )
    // {
    // this.library = library;
    // this.autonxConfig = autonxConfig;
    // this.embConfig = embConfig;
    // this.prompter = prompter;
    // }

    public Map<String, String> resolveFromNexusUrl( final String nexusUrl )
    {
        return resolveFromNexusUrls( Collections.singleton( nexusUrl ) );
    }

    public Map<String, String> resolveFromNexusUrls( final Set<String> nexusUrls )
    {
        final Map<String, String> result = new LinkedHashMap<String, String>();
        if ( autonxConfig.isDisabled() )
        {
            return result;
        }

        if ( nexusUrls != null && !nexusUrls.isEmpty() )
        {
            final StringBuilder builder = new StringBuilder();

            for ( final String baseUrl : nexusUrls )
            {
                builder.setLength( 0 );
                builder.append( baseUrl );
                if ( !baseUrl.endsWith( "/" ) )
                {
                    builder.append( '/' );
                }

                builder.append( "service/local/autonx/mirrors" );

                if ( library.getLogger().isDebugEnabled() )
                {
                    library.getLogger().debug( "Grabbing mirror mappings from: " + builder.toString() );
                }

                final HttpGet get = new HttpGet( builder.toString() );
                get.addHeader( "Accept", "text/plain;q=0.9,*/*;q=0.8" );

                try
                {
                    client.execute( get, new ResponseHandler<Void>()
                    {
                        public Void handleResponse( final HttpResponse response )
                            throws /* ClientProtocolException, */IOException
                        {
                            final int statusCode = response.getStatusLine().getStatusCode();
                            if ( statusCode > 199 && statusCode < 204 )
                            {
                                InputStream stream = null;
                                try
                                {
                                    stream = response.getEntity().getContent();
                                    final BufferedReader br = new BufferedReader( new InputStreamReader( stream ) );

                                    String line = null;
                                    while ( ( line = br.readLine() ) != null )
                                    {
                                        if ( isNotBlank( line ) )
                                        {
                                            final int idx = line.indexOf( '=' );
                                            if ( idx > 0 )
                                            {
                                                final String repoUrl = line.substring( 0, idx );
                                                final String mirrorUrl = line.substring( idx + 1 );

                                                if ( library.getLogger().isDebugEnabled() )
                                                {
                                                    library.getLogger().debug( "Resolved-Mirrors += " + repoUrl
                                                                                               + "\n\t=> " + mirrorUrl );
                                                }

                                                result.put( repoUrl, mirrorUrl );
                                            }
                                        }
                                    }
                                }
                                finally
                                {
                                    close( stream );
                                }
                            }
                            else if ( library.getLogger().isDebugEnabled() )
                            {
                                library.getLogger()
                                       .debug( "Response: " + response.getStatusLine().getStatusCode() + " "
                                                               + response.getStatusLine().getReasonPhrase() );
                            }

                            return null;
                        }
                    } );
                }
                catch ( final ClientProtocolException e )
                {
                    if ( library.getLogger().isDebugEnabled() )
                    {
                        library.getLogger().debug( "Failed to read proxied repositories from: '" + builder.toString()
                                                                   + "'. Reason: " + e.getMessage(), e );
                    }
                }
                catch ( final IOException e )
                {
                    if ( library.getLogger().isDebugEnabled() )
                    {
                        library.getLogger().debug( "Failed to read proxied repositories from: '" + builder.toString()
                                                                   + "'. Reason: " + e.getMessage(), e );
                    }
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    @Override
    public synchronized void initialize()
        throws InitializationException
    {
        if ( initialized )
        {
            return;
        }

        final DefaultHttpClient client = new DefaultHttpClient();
        this.client = client;

        client.setCredentialsProvider( new CredentialsProvider()
        {
            private final Map<String, UsernamePasswordCredentials> cached =
                new HashMap<String, UsernamePasswordCredentials>();

            public void setCredentials( final AuthScope authscope, final Credentials credentials )
            {
            }

            public synchronized Credentials getCredentials( final AuthScope authscope )
            {
                UsernamePasswordCredentials creds = autonxConfig.getNexusCredentials();
                if ( creds == null && embConfig.isInteractive() )
                {
                    final String key = authscope.getHost() + ":" + authscope.getPort();

                    creds = cached.get( key );
                    if ( creds == null )
                    {
                        final StringBuilder sb =
                            new StringBuilder().append( authscope.getRealm() )
                                               .append( " (" )
                                               .append( key )
                                               .append( ") requires authentication.\n\nUsername" );

                        try
                        {
                            final String user = prompter.prompt( sb.toString() );
                            final String password = prompter.promptForPassword( "Password" );

                            creds = new UsernamePasswordCredentials( user, password );
                            cached.put( key, creds );
                        }
                        catch ( final PrompterException e )
                        {
                            if ( library.getLogger().isDebugEnabled() )
                            {
                                library.getLogger().debug( "Failed to read credentials! Reason: " + e.getMessage(), e );
                            }
                        }
                    }
                }

                return creds;
            }

            public void clear()
            {
            }
        } );

        initialized = true;
    }
}
