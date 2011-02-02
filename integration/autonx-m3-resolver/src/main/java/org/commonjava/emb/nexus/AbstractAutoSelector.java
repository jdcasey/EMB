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

package org.commonjava.emb.nexus;

import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.nexus.conf.AutoNXConfiguration;
import org.commonjava.emb.nexus.resolve.AutoMirrorResolver;
import org.commonjava.emb.nexus.search.FailoverDiscovery;
import org.commonjava.emb.nexus.search.MirrorDiscoveryStrategy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractAutoSelector
    implements Initializable
{

    protected final Map<String, String> autodetectedMirrors = new HashMap<String, String>();

    private boolean initialized = false;

    @Requirement( hint = "autonx" )
    protected EMBLibrary library;

    @Requirement
    protected AutoNXConfiguration autonxConfig;

    @Requirement( role = MirrorDiscoveryStrategy.class )
    private List<MirrorDiscoveryStrategy> strategies;

    @Requirement
    private FailoverDiscovery failover;

    @Requirement
    private AutoMirrorResolver mirrorResolver;

    public synchronized void initialize()
        throws InitializationException
    {
        if ( initialized )
        {
            return;
        }

        if ( !autonxConfig.isDisabled() )
        {
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
                    for ( final MirrorDiscoveryStrategy strategy : strategies )
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

                if ( candidates.isEmpty() )
                {
                    candidates.addAll( failover.findNexusCandidates() );
                }

                for ( final Map.Entry<String, String> entry : mirrorResolver.resolveFromNexusUrls( candidates )
                                                                            .entrySet() )
                {
                    final String repoUrl = entry.getKey();
                    final String mirrorUrl = entry.getValue();

                    if ( !autodetectedMirrors.containsKey( repoUrl ) )
                    {
                        if ( library.getLogger().isDebugEnabled() )
                        {
                            library.getLogger().debug( "Auto-Mirrors += " + repoUrl + "\n\t=> " + mirrorUrl );
                        }

                        autodetectedMirrors.put( repoUrl, mirrorUrl );
                        if ( repoUrl.endsWith( "/" ) )
                        {
                            autodetectedMirrors.put( repoUrl.substring( 0, repoUrl.length() - 1 ), mirrorUrl );
                        }
                    }
                    else
                    {
                        if ( library.getLogger().isDebugEnabled() )
                        {
                            library.getLogger().debug( "Auto-Mirrors already contains key: " + repoUrl );
                        }
                    }
                }
            }
            catch ( final AutoNXException e )
            {
                if ( library.getLogger().isDebugEnabled() )
                {
                    library.getLogger().error( "Failed to auto-detect Nexus mirrors: " + e.getMessage(), e );
                }
            }
        }

        initialized = true;
    }

    protected Logger getLogger()
    {
        return library.getLogger();
    }

}
