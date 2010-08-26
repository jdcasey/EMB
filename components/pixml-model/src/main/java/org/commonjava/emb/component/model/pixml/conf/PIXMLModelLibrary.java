package org.commonjava.emb.component.model.pixml.conf;

import org.apache.maven.model.io.ModelReader;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.plexus.ComponentSelector;

/**
 * EMB library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( EMBLibrary.class )
public class PIXMLModelLibrary
    extends AbstractEMBLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public PIXMLModelLibrary()
    {
        super( "pixml", "PI-Aware-XML-Model", new MavenPomVersionProvider( "org.commonjava.emb.components",
                                                                           "emb-pixml-model" ),
               new ComponentSelector().setSelection( ModelReader.class, "pixml" ) );
    }
}
