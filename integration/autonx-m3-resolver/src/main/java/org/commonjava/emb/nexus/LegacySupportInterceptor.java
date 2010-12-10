package org.commonjava.emb.nexus;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

@Component( role = LegacySupport.class, hint = "autonx" )
public class LegacySupportInterceptor
    implements LegacySupport
{

    @Requirement( hint = "default_" )
    private LegacySupport delegate;

    @Requirement
    private PlexusContainer container;

    @Requirement( role = MirrorSelector.class, hint = "autonx" )
    private AetherAutoSelector autonxSelector;

    @Override
    public void setSession( final MavenSession session )
    {
        final RepositorySystemSession original = session.getRepositorySession();
        autonxSelector.setDelegateSession( original );

        final DefaultRepositorySystemSession replacement = new DefaultRepositorySystemSession( original );
        replacement.setMirrorSelector( autonxSelector );

        delegate.setSession( new MavenSession( container, replacement, session.getRequest(), session.getResult() ) );
    }

    @Override
    public MavenSession getSession()
    {
        return delegate.getSession();
    }

    @Override
    public RepositorySystemSession getRepositorySession()
    {
        return delegate.getRepositorySession();
    }

}
