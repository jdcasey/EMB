package org.commonjava.emb.conf.loader;

import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class InstanceLibraryLoader
    implements EMBLibraryLoader
{

    private final List<EMBLibrary> libraries;

    public InstanceLibraryLoader( final EMBLibrary... libraries )
    {
        this.libraries =
            libraries == null ? new ArrayList<EMBLibrary>() : new ArrayList<EMBLibrary>( Arrays.asList( libraries ) );
    }

    public InstanceLibraryLoader( final List<EMBLibrary> libraries )
    {
        this.libraries = libraries;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.conf.loader.EMBLibraryLoader#loadLibraries(org.commonjava.emb.conf.EMBConfiguration)
     */
    @Override
    public Collection<EMBLibrary> loadLibraries( final EMBConfiguration embConfig )
        throws IOException
    {
        return libraries;
    }

}
