package org.commonjava.emb.component.vscheme.conf;

import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.component.vscheme.VersionSchemeSelector;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.conf.mgmt.LoadOnFinish;
import org.commonjava.emb.conf.mgmt.LoadOnStart;
import org.commonjava.emb.plexus.ComponentKey;

/**
 * EMB library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( EMBLibrary.class )
public class VersionSchemeInjectorLibrary
    extends AbstractEMBLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public VersionSchemeInjectorLibrary()
    {
        super( "vscheme", "Version-Scheme-Injector", new MavenPomVersionProvider( "org.commonjava.emb.components",
                                                                                  "version-scheme-injector" ) );

        withManagementComponent( new ComponentKey( VersionSchemeSelector.class ), LoadOnStart.class, LoadOnFinish.class );
    }
}
