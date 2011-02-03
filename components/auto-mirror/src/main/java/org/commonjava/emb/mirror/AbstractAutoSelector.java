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

package org.commonjava.emb.mirror;

import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.mirror.conf.AutoMirrorConfiguration;
import org.commonjava.emb.mirror.conf.AutoMirrorLibrary;
import org.commonjava.emb.mirror.model.RouterMirrorsMapping;
import org.commonjava.emb.mirror.resolve.AutoMirrorResolver;
import org.commonjava.emb.mirror.search.RouterDiscoveryStrategy;

import java.util.List;

abstract class AbstractAutoSelector
    implements Initializable
{

    protected RouterMirrorsMapping mirrorMapping;

    private boolean initialized = false;

    @Requirement( hint = AutoMirrorLibrary.HINT )
    protected EMBLibrary library;

    @Requirement
    protected AutoMirrorConfiguration config;

    @Requirement( role = RouterDiscoveryStrategy.class )
    private List<RouterDiscoveryStrategy> strategies;

    @Requirement
    private AutoMirrorResolver mirrorResolver;

    public synchronized void initialize()
        throws InitializationException
    {
        if ( initialized )
        {
            return;
        }

        if ( !config.isDisabled() )
        {
            try
            {
                if ( config.getRouterUrl() != null )
                {
                    mirrorMapping = mirrorResolver.getMirrorMapping( config.getRouterUrl() );
                }
                else
                {
                    String routerUrl = null;
                    for ( final RouterDiscoveryStrategy strategy : strategies )
                    {
                        if ( strategy == null )
                        {
                            continue;
                        }

                        routerUrl = strategy.findRouter();
                        if ( routerUrl != null && !routerUrl.trim().isEmpty() )
                        {
                            mirrorMapping = mirrorResolver.getMirrorMapping( routerUrl );
                            if ( mirrorMapping != null )
                            {
                                break;
                            }
                        }
                    }
                }

                final String centralRouterUrl = config.getCanonicalRouterUrl();
                if ( mirrorMapping == null && centralRouterUrl != null && centralRouterUrl.trim().length() > 0 )
                {
                    mirrorMapping = mirrorResolver.getMirrorMapping( config.getRouterUrl() );
                }
            }
            catch ( final AutoMirrorException e )
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
