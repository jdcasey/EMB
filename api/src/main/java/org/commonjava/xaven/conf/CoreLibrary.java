package org.commonjava.xaven.conf;

import org.commonjava.atservice.annotation.Service;

/**
 * Xaven library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( XavenLibrary.class )
public class CoreLibrary
    extends AbstractXavenLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public CoreLibrary()
    {
        super( "core", "Xaven-Core", new MavenPomVersionProvider( "org.commonjava.xaven", "xaven-api" ) );
    }
}
