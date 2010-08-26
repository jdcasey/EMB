package org.commonjava.emb.component.vscheme;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;

import java.util.List;

@Component( role = VersionSchemeManager.class )
public class VersionSchemeManager
    implements MirrorSelector, Initializable
{

    @SuppressWarnings( "unused" )
    @Requirement( hint = "#" )
    private MirrorSelector delegateSelector;

    @SuppressWarnings( "unused" )
    @Requirement( hint = "version-scheme-injector" )
    private EMBLibrary library;

    @SuppressWarnings( "unused" )
    @Requirement
    private EMBConfiguration embConfig;

    @Override
    public void initialize()
        throws InitializationException
    {
        // Normally, this is where you might initialize the mirrors available to EMB.
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    @Override
    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        // Select the appropriate mirror from those detected.
        throw new UnsupportedOperationException( "Not Implemented." );
    }

}
