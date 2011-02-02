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

package org.commonjava.emb.nexus;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.repository.MirrorSelector;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.util.List;

@Component( role = MirrorSelector.class, hint = "autonx" )
public class MavenAutoSelector
    extends AbstractAutoSelector
    implements MirrorSelector
{

    @Requirement( hint = "default_" )
    private MirrorSelector delegateSelector;

    public Mirror getMirror( final ArtifactRepository repository, final List<Mirror> mirrors )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "MAVEN-SELECT: " + repository.getUrl() );
        }

        Mirror mirror = delegateSelector.getMirror( repository, mirrors );

        if ( mirror == null && !autonxConfig.isDisabled() )
        {
            final String repoUrl = repository.getUrl();
            final String mirrorUrl = autodetectedMirrors.get( repoUrl );
            if ( mirrorUrl != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "\t\t====> " + mirrorUrl );
                }

                mirror = new Mirror();
                mirror.setMirrorOf( repository.getId() );
                mirror.setLayout( "default" );
                mirror.setId( autonxConfig.getMirrorId() );
                mirror.setUrl( mirrorUrl );
            }
            else
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "MAVEN-SELECT: no auto-mirror found." );
                }
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "MAVEN-SELECT disabled." );
            }
        }

        return mirror;
    }

}
