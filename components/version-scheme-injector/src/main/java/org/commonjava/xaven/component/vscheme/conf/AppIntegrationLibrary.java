package org.commonjava.xaven.component.vscheme.conf;

import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentSelector;

/**
 * Xaven library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( XavenLibrary.class )
public class AppIntegrationLibrary
    extends AbstractXavenLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public AppIntegrationLibrary()
    {
        super( "version-scheme-injector", "version-scheme-injector", new MavenPomVersionProvider( "org.commonjava.xaven.components", "version-scheme-injector" ),
               new AppIntegrationConfigLoader(), new ComponentSelector().setSelection( MirrorSelector.class, "app" ) );
    }
}
