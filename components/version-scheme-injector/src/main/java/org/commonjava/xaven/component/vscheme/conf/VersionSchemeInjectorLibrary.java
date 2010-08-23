package org.commonjava.xaven.component.vscheme.conf;

import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.component.vscheme.VersionSchemeSelector;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.conf.mgmt.LoadOnFinish;
import org.commonjava.xaven.conf.mgmt.LoadOnStart;
import org.commonjava.xaven.plexus.ComponentKey;

/**
 * Xaven library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( XavenLibrary.class )
public class VersionSchemeInjectorLibrary
    extends AbstractXavenLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public VersionSchemeInjectorLibrary()
    {
        super( "vscheme", "Version-Scheme-Injector", new MavenPomVersionProvider( "org.commonjava.xaven.components",
                                                                                  "version-scheme-injector" ) );

        withManagementComponent( new ComponentKey( VersionSchemeSelector.class ), LoadOnStart.class, LoadOnFinish.class );
    }
}
