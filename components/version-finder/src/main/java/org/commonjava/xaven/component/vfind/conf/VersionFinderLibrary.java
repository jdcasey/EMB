package org.commonjava.xaven.component.vfind.conf;

import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.component.vfind.VersionFinder;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentKey;

@Service( XavenLibrary.class )
public class VersionFinderLibrary
    extends AbstractXavenLibrary
{

    public static final String KEY = "vfind";

    // Requires an empty constructor for ServiceLoader to work!
    public VersionFinderLibrary()
    {
        super( KEY, "Version-Finder", new MavenPomVersionProvider( "org.commonjava.xaven.components",
                                                                   "xaven-version-finder" ) );
        withExportedComponent( new ComponentKey( VersionFinder.class ) );
    }
}
