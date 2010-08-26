package org.commonjava.emb.nexus.search;

import org.commonjava.emb.nexus.AutoNXException;

import java.util.LinkedHashSet;

public interface NexusDiscoveryStrategy
{

    LinkedHashSet<String> findNexusCandidates()
        throws AutoNXException;

}
