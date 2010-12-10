package org.commonjava.emb.nexus;

import static org.junit.Assert.assertTrue;

import org.apache.maven.repository.MirrorSelector;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.internal.plexus.EMBPlexusContainer;
import org.commonjava.emb.nexus.conf.AutoNXLibrary;
import org.commonjava.emb.nexus.fixture.MirrorSelectorUser;
import org.junit.Test;

import java.io.File;

public class NexusAutoMirrorSelectorTest
{

    @Test
    public void initializedProperly()
        throws Throwable
    {
        final ContainerConfiguration config = new DefaultContainerConfiguration().setClassPathScanning( true );
        final AutoNXLibrary lib = new AutoNXLibrary();

        final EMBConfiguration embConfig = new EMBConfiguration().withConfigurationDirectory( getResourceFile( "m2" ) );

        lib.loadConfiguration( embConfig );

        lib.getInstanceRegistry().add( EMBLibrary.class, "autonx", lib );
        lib.getInstanceRegistry().add( lib.getConfiguration() );
        lib.getInstanceRegistry().add( embConfig );

        final EMBPlexusContainer container =
            new EMBPlexusContainer( config, lib.getComponentSelector(), lib.getInstanceRegistry() );

        final MirrorSelectorUser user = container.lookup( MirrorSelectorUser.class );
        final MirrorSelector mirrorSelector = user.mirrorSelector();

        assertTrue( mirrorSelector instanceof AbstractAutoSelector );
    }

    private File getResourceFile( final String path )
    {
        return new File( Thread.currentThread().getContextClassLoader().getResource( path ).getPath() );
    }

}
