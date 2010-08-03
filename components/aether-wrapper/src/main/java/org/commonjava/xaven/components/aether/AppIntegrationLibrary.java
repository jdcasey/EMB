package org.commonjava.xaven.components.aether;

import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentKey;
import org.sonatype.aether.RepositorySystem;

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
        super( "aether-wrapper", "aether-wrapper", new MavenPomVersionProvider( "org.commonjava.xaven.components",
                                                                                "aether-wrapper" ) );

        withExportedComponent( new ComponentKey( RepositorySystem.class ) );
    }
}
