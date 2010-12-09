package org.commonjava.emb.nexus.search;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.nexus.AutoNXException;
import org.commonjava.emb.nexus.conf.AutoNXConfiguration;
import org.commonjava.emb.nexus.nx.AutoMirrorResolver;

import java.util.HashMap;
import java.util.Map;

@Component( role = FailOverDiscoveryStrategy.class, hint = "simple" )
public class SimpleFailoverDiscoveryStrategy
    implements FailOverDiscoveryStrategy, Initializable
{

    private final Map<String, String> autodetectedMirrors = new HashMap<String, String>();

    private boolean initialized = false;

    @Requirement
    private AutoNXConfiguration autonxConfig;

    @Requirement
    private AutoMirrorResolver mirrorPopulator;

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.nexus.search.FailOverDiscoveryStrategy#getMirrorUrl(java.lang.String)
     */
    @Override
    public String getMirrorUrl( final String url )
        throws AutoNXException
    {
        return autodetectedMirrors.get( url );
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

        autodetectedMirrors.putAll( mirrorPopulator.resolveFromNexusUrl( autonxConfig.getFailoverNexusUrl() ) );
        initialized = true;
    }

}
