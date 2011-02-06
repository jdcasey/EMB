package org.commonjava.emb.mirror;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.mirror.conf.AutoMirrorLibrary;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

@Component( role = LegacySupport.class, hint = AutoMirrorLibrary.HINT )
public class LegacySupportInterceptor
    implements LegacySupport
{

    @Requirement( hint = "default_" )
    private LegacySupport delegate;

    @Requirement
    private PlexusContainer container;

    @Requirement( role = MirrorSelector.class, hint = AutoMirrorLibrary.HINT )
    private AetherAutoSelector autonxSelector;

    @Override
    public void setSession( final MavenSession session )
    {
        MirrorSelector original = null;

        boolean setDelegate = true;
        if ( session != null && session.getRepositorySession() != null )
        {
            final RepositorySystemSession repoSession = session.getRepositorySession();
            original = repoSession.getMirrorSelector();

            if ( original instanceof AetherAutoSelector )
            {
                // pass-through.
                delegate.setSession( session );
                setDelegate = false;
            }
            else if ( repoSession instanceof DefaultRepositorySystemSession )
            {
                ( (DefaultRepositorySystemSession) repoSession ).setMirrorSelector( autonxSelector );
                delegate.setSession( session );
            }
            else
            {
                final DefaultRepositorySystemSession replacement = new DefaultRepositorySystemSession( repoSession );
                replacement.setMirrorSelector( autonxSelector );

                delegate.setSession( new MavenSession( container, replacement, session.getRequest(),
                                                       session.getResult() ) );
            }
        }

        if ( setDelegate )
        {
            autonxSelector.setDelegateSelector( original );
        }
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
