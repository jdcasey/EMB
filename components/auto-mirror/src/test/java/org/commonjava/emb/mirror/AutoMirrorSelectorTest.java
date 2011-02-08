package org.commonjava.emb.mirror;

import static org.junit.Assert.assertTrue;

import org.apache.maven.repository.MirrorSelector;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.internal.plexus.EMBPlexusContainer;
import org.commonjava.emb.mirror.conf.AutoMirrorConfiguration;
import org.commonjava.emb.mirror.conf.AutoMirrorLibrary;
import org.commonjava.emb.mirror.fixture.MirrorSelectorUser;
import org.junit.Test;

import java.io.File;

public class AutoMirrorSelectorTest
{

    @Test
    public void initializedProperly()
        throws Throwable
    {
        final ContainerConfiguration config = new DefaultContainerConfiguration().setClassPathScanning( true );

        final AutoMirrorConfiguration autoMirrorConfig =
            new AutoMirrorConfiguration().setDiscoveryStrategies( AutoMirrorConfiguration.NO_DISCOVERY_STRATEGIES );

        final AutoMirrorLibrary lib = new AutoMirrorLibrary( autoMirrorConfig );

        final EMBConfiguration embConfig = new EMBConfiguration().withConfigurationDirectory( getResourceFile( "m2" ) );

        lib.getInstanceRegistry().add( EMBLibrary.class, AutoMirrorLibrary.HINT, lib );
        lib.getInstanceRegistry().add( autoMirrorConfig );
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