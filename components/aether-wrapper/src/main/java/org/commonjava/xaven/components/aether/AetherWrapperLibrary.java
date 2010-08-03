package org.commonjava.xaven.components.aether;

import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentKey;

@Service( XavenLibrary.class )
public class AetherWrapperLibrary
    extends AbstractXavenLibrary
{
    public AetherWrapperLibrary()
    {
        super( "aether-wrapper", "aether-wrapper", new MavenPomVersionProvider( "org.commonjava.xaven.components",
                                                                                "aether-wrapper" ) );

        withExportedComponent( new ComponentKey( VersionFinder.class ) );
    }
}
