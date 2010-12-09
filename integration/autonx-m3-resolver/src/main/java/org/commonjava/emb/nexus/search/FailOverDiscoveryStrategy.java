package org.commonjava.emb.nexus.search;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.nexus.AutoNXException;
import org.commonjava.emb.nexus.conf.AutoNXConfiguration;

import java.util.Collections;
import java.util.LinkedHashSet;

@Component( role = NexusDiscoveryStrategy.class, hint = "failover" )
public class FailoverDiscoveryStrategy
    implements NexusDiscoveryStrategy
{

    @Requirement
    private AutoNXConfiguration autonxConfig;

    @Override
    public LinkedHashSet<String> findNexusCandidates()
        throws AutoNXException
    {
        return new LinkedHashSet<String>( Collections.singleton( autonxConfig.getFailoverNexusUrl() ) );
    }

}
