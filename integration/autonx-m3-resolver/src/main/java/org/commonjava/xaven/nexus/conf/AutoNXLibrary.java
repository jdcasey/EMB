package org.commonjava.xaven.nexus.conf;

import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentSelector;

@Service( XavenLibrary.class )
public class AutoNXLibrary
    extends AbstractXavenLibrary
{

    public AutoNXLibrary()
    {
        super( "autonx", "AutoNX-Mirror-Selector", new MavenPomVersionProvider( "org.commonjava.xaven.integration",
                                                                                "xaven-autonx-m3-resolver" ), "autonx",
               new AutoNXConfigLoader(), new ComponentSelector().setSelection( MirrorSelector.class, "autonx" ) );
    }

}
