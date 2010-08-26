package org.commonjava.emb.component.vfind.conf;

import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.component.vfind.VersionFinder;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.plexus.ComponentKey;

@Service( EMBLibrary.class )
public class VersionFinderLibrary
    extends AbstractEMBLibrary
{

    public static final String KEY = "vfind";

    // Requires an empty constructor for ServiceLoader to work!
    public VersionFinderLibrary()
    {
        super( KEY, "Version-Finder", new MavenPomVersionProvider( "org.commonjava.emb.components",
                                                                   "emb-version-finder" ) );
        withExportedComponent( new ComponentKey( VersionFinder.class ) );
    }
}
