#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import ${package}.conf.AppIntegrationConfiguration;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.conf.XavenLibrary;

import java.util.List;

@Component( role = MirrorSelector.class, hint = "app" )
public class AppMirrorSelector
    implements MirrorSelector, Initializable
{

    @SuppressWarnings( "unused" )
    @Requirement( hint = "${symbol_pound}" )
    private MirrorSelector delegateSelector;

    @SuppressWarnings( "unused" )
    @Requirement( hint = "${artifactId}" )
    private XavenLibrary library;

    @SuppressWarnings( "unused" )
    @Requirement
    private AppIntegrationConfiguration appConfig;

    @SuppressWarnings( "unused" )
    @Requirement
    private XavenConfiguration xavenConfig;

    @Override
    public void initialize()
        throws InitializationException
    {
        // Normally, this is where you might initialize the mirrors available to Xaven.
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    @Override
    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        // Select the appropriate mirror from those detected.
        throw new UnsupportedOperationException( "Not Implemented." );
    }

}