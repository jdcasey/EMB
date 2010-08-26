package org.commonjava.emb.nexus.conf;

import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.plexus.ComponentSelector;

@Service( EMBLibrary.class )
public class AutoNXLibrary
    extends AbstractEMBLibrary
{

    public AutoNXLibrary()
    {
        super( "autonx", "AutoNX-Mirror-Selector", new MavenPomVersionProvider( "org.commonjava.emb.integration",
                                                                                "emb-autonx-m3-resolver" ),
               new AutoNXConfigLoader(), new ComponentSelector().setSelection( MirrorSelector.class, "autonx" ) );
    }

}
