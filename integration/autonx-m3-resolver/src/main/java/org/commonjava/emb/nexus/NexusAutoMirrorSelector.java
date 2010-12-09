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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.nexus.conf.AutoNXConfiguration;
import org.commonjava.emb.nexus.nx.AutoMirrorResolver;
import org.commonjava.emb.nexus.search.FailOverDiscoveryStrategy;
import org.commonjava.emb.nexus.search.NexusDiscoveryStrategy;

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

    @Requirement( hint = "default_" )
    private MirrorSelector delegateSelector;

    @Requirement( hint = "autonx" )
    private EMBLibrary library;

    @Requirement
    private AutoNXConfiguration autonxConfig;

    @Requirement( role = NexusDiscoveryStrategy.class )
    private List<NexusDiscoveryStrategy> strategies;

    @Requirement( optional = true )
    private FailOverDiscoveryStrategy failover;

    @Requirement
    private AutoMirrorResolver mirrorPopulator;

    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        final String repoUrl = repository.getUrl();

        String mirrorUrl = autodetectedMirrors.get( repoUrl );
        if ( mirrorUrl == null && failover != null )
        {
            try
            {
                mirrorUrl = failover.getMirrorUrl( repoUrl );
            }
            catch ( final AutoNXException e )
            {
                if ( library.getLogger().isDebugEnabled() )
                {
                    library.getLogger().error( "Failed to auto-detect Nexus mirror for: " + repoUrl + ".\nReason: "
                                                               + e.getMessage(), e );
                }

                mirrorUrl = null;
            }
        }

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

            autodetectedMirrors.putAll( mirrorPopulator.resolveFromNexusUrls( candidates ) );
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

}
