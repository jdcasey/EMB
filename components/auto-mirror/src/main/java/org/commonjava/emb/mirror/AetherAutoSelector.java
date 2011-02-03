/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.mirror;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.codehaus.plexus.component.annotations.Component;
import org.commonjava.emb.mirror.conf.AutoMirrorLibrary;
import org.commonjava.emb.mirror.model.RouterMirror;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.MirrorSelector;
import org.sonatype.aether.repository.RemoteRepository;

import java.util.Collections;

@Component( role = MirrorSelector.class, hint = AutoMirrorLibrary.HINT )
public class AetherAutoSelector
    extends AbstractAutoSelector
    implements MirrorSelector
{

    private MirrorSelector delegate;

    public AetherAutoSelector setDelegateSelector( final MirrorSelector delegate )
    {
        this.delegate = delegate;
        return this;
    }

    @Override
    public RemoteRepository getMirror( final RemoteRepository repository )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "AETHER-SELECT: " + repository.getUrl() );
        }

        RemoteRepository mirror = null;

        if ( delegate != null )
        {
            mirror = delegate.getMirror( repository );
        }

        if ( mirror == null && !config.isDisabled() )
        {
            final String repoUrl = repository.getUrl();

            RouterMirror rMirror = mirrorMapping.getSelectedMirror( repoUrl );
            if ( rMirror == null )
            {
                rMirror = mirrorMapping.getWeightedRandomSuggestion( repoUrl );
            }

            if ( rMirror != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "\t\t====> " + rMirror );
                }

                mirror = new RemoteRepository();

                mirror.setRepositoryManager( true );
                mirror.setId( rMirror.getId() );
                mirror.setUrl( rMirror.getUrl() );
                mirror.setContentType( repository.getContentType() );
                mirror.setPolicy( true, repository.getPolicy( true ) );
                mirror.setPolicy( false, repository.getPolicy( false ) );

                final UsernamePasswordCredentials creds = config.getRouterCredentials();
                mirror.setAuthentication( new Authentication( creds.getUserName(), creds.getPassword() ) );

                mirror.setMirroredRepositories( Collections.singletonList( repository ) );
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "AETHER-SELECT: no auto-mirror found." );
                }
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "AETHER-SELECT disabled." );
            }
        }

        return mirror;
    }
}
