package org.commonjava.emb.conf;

import org.commonjava.atservice.annotation.Service;

/**
 * EMB library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( EMBLibrary.class )
public class CoreLibrary
    extends AbstractEMBLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public CoreLibrary()
    {
        super( "core", "EMB-Core", new MavenPomVersionProvider( "org.commonjava.emb", "emb-api" ) );
    }
}
