package org.commonjava.emb.conf.loader;

import org.apache.log4j.Logger;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;

public class ServiceLibraryLoader
    implements EMBLibraryLoader
{

    @SuppressWarnings( "unused" )
    private static final Logger logger = Logger.getLogger( EMBConfiguration.STANDARD_LOG_HANDLE_LOADER );

    public Collection<EMBLibrary> loadLibraries( final EMBConfiguration embConfig )
        throws IOException
    {
        final LinkedHashSet<EMBLibrary> libraries = new LinkedHashSet<EMBLibrary>();

        final ServiceLoader<EMBLibrary> loader = ServiceLoader.load( EMBLibrary.class );
        for ( final EMBLibrary library : loader )
        {
            libraries.add( library );
        }

        return libraries;
    }

}
