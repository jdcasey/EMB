package org.commonjava.xaven.nexus.search;

import org.commonjava.xaven.nexus.AutoNXException;

import java.util.LinkedHashSet;

public interface NexusDiscoveryStrategy
{

    LinkedHashSet<String> findNexusCandidates()
        throws AutoNXException;

}
