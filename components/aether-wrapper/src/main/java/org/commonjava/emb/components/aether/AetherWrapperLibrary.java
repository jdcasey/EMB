package org.commonjava.emb.components.aether;

import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.plexus.ComponentKey;

@Service( EMBLibrary.class )
public class AetherWrapperLibrary
    extends AbstractEMBLibrary
{
    public AetherWrapperLibrary()
    {
        super( "aether-wrapper", "aether-wrapper", new MavenPomVersionProvider( "org.commonjava.emb.components",
                                                                                "aether-wrapper" ) );

        withExportedComponent( new ComponentKey( VersionFinder.class ) );
    }
}
