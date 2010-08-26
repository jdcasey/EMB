#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.conf;

import org.apache.maven.repository.MirrorSelector;
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
public class AppIntegrationLibrary
    extends AbstractEMBLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public AppIntegrationLibrary()
    {
        super( "${artifactId}", "${artifactId}", new MavenPomVersionProvider( "${groupId}", "${artifactId}" ),
               new AppIntegrationConfigLoader(), new ComponentSelector().setSelection( MirrorSelector.class, "app" ) );
    }
}
