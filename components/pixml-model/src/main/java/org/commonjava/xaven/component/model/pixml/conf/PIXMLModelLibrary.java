package org.commonjava.xaven.component.model.pixml.conf;

import org.apache.maven.model.io.ModelReader;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentSelector;

/**
 * Xaven library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( XavenLibrary.class )
public class PIXMLModelLibrary
    extends AbstractXavenLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public PIXMLModelLibrary()
    {
        super( "pixml", "PI-Aware-XML-Model", new MavenPomVersionProvider( "org.commonjava.xaven.components",
                                                                           "xaven-pixml-model" ),
               new ComponentSelector().setSelection( ModelReader.class, "pixml" ) );
    }
}
