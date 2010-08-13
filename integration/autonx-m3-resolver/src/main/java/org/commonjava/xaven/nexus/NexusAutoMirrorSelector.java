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

import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.nexus.conf.AutoNXConfiguration;
import org.commonjava.xaven.nexus.search.NexusDiscoveryStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component( role = MirrorSelector.class, hint = "autonx" )
public class NexusAutoMirrorSelector
    implements MirrorSelector, Initializable
{

    private final Map<String, String> autodetectedMirrors = new HashMap<String, String>();

    private boolean initialized = false;

    //    @Requirement
    //    private LegacySupport legacySupport;

    @Requirement( hint = "default_" )
    private MirrorSelector delegateSelector;

    @Requirement( hint = "autonx" )
    private XavenLibrary library;

    @Requirement
    private AutoNXConfiguration autonxConfig;

    @Requirement
    private XavenConfiguration xavenConfig;

    @Requirement( hint = "xaven" )
    private Prompter prompter;

    @Requirement( role = NexusDiscoveryStrategy.class )
    private List<NexusDiscoveryStrategy> strategies;

    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        final String mirrorUrl = autodetectedMirrors.get( repository.getUrl() );

        Mirror mirror = null;
        if ( mirrorUrl != null )
        {
            mirror = new Mirror();
            mirror.setMirrorOf( repository.getId() );
            mirror.setLayout( "default" );
            mirror.setId( autonxConfig.getMirrorId() );
            mirror.setUrl( mirrorUrl );
        }
        else
        {
            mirror = delegateSelector.getMirror( repository, mirrors );
        }

        return mirror;
    }

    public synchronized void initialize()
        throws InitializationException
    {
        if ( initialized )
        {
            return;
        }

        try
        {
            final Set<String> candidates = new LinkedHashSet<String>();

            if ( autonxConfig.getNexusUrl() != null )
            {
                candidates.add( autonxConfig.getNexusUrl() );
            }
            else
            {
                LinkedHashSet<String> tmp = null;
                for ( final NexusDiscoveryStrategy strategy : strategies )
                {
                    if ( strategy == null )
                    {
                        continue;
                    }

                    tmp = strategy.findNexusCandidates();
                    if ( tmp != null && !tmp.isEmpty() )
                    {
                        candidates.addAll( tmp );
                    }
                }
            }

            populateAutoMirrors( candidates );
        }
        catch ( final AutoNXException e )
        {
            if ( library.getLogger().isDebugEnabled() )
            {
                library.getLogger().error( "Failed to auto-detect Nexus mirrors: " + e.getMessage(), e );
            }
        }

        initialized = true;
    }

    private void populateAutoMirrors( final Set<String> candidates )
    {
        if ( candidates != null && !candidates.isEmpty() )
        {
            final DefaultHttpClient client = new DefaultHttpClient();

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
                    if ( creds == null && xavenConfig.isInteractive() )
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
                                    library.getLogger().debug( "Failed to read credentials! Reason: " + e.getMessage(),
                                                               e );
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

            final StringBuilder builder = new StringBuilder();

            for ( final String baseUrl : candidates )
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
                try
                {
                    final Map<String, String> mirrors = client.execute( get, new ResponseHandler<Map<String, String>>()
                    {
                        public Map<String, String> handleResponse( final HttpResponse response )
                            throws /*ClientProtocolException,*/IOException
                        {
                            final int statusCode = response.getStatusLine().getStatusCode();
                            if ( statusCode > 199 && statusCode < 300 )
                            {
                                final Map<String, String> mirrors = new HashMap<String, String>();

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
                                                    library.getLogger().debug( "Mirroring: " + repoUrl + "\n\t==> "
                                                                                   + mirrorUrl );
                                                }

                                                mirrors.put( repoUrl, mirrorUrl );
                                            }
                                        }
                                    }
                                }
                                finally
                                {
                                    close( stream );
                                }

                                return mirrors;
                            }
                            else if ( library.getLogger().isDebugEnabled() )
                            {
                                library.getLogger().debug( "Response: " + response.getStatusLine().getStatusCode()
                                                               + " " + response.getStatusLine().getReasonPhrase() );
                            }

                            return null;
                        }
                    } );

                    if ( mirrors != null && !mirrors.isEmpty() )
                    {
                        autodetectedMirrors.putAll( mirrors );
                    }
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
    }

}
