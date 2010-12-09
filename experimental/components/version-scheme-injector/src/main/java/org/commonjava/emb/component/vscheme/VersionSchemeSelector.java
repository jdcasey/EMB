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

package org.commonjava.emb.component.vscheme;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.commonjava.emb.EMBAdvisor;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.conf.mgmt.LoadOnFinish;
import org.commonjava.emb.conf.mgmt.LoadOnStart;
import org.commonjava.emb.conf.mgmt.EMBManagementView;

import java.util.Map;

@Component( role = VersionSchemeSelector.class )
public class VersionSchemeSelector
    implements LoadOnStart, LoadOnFinish
{

    private static InheritableThreadLocal<VersionSchemeSelector> instance =
        new InheritableThreadLocal<VersionSchemeSelector>();

    @Requirement( role = VersionScheme.class )
    private Map<String, VersionScheme> schemes;

    @Requirement( hint = VersionScheme.DEFAULT_KEY )
    private VersionScheme defaultVersionScheme;

    @Requirement( hint = "vscheme" )
    private EMBLibrary library;

    @Requirement
    private EMBAdvisor advisor;

    public static VersionScheme getTLCurrentVersionScheme()
    {
        return selector().getCurrentVersionScheme();
    }

    private static VersionSchemeSelector selector()
    {
        final VersionSchemeSelector sel = instance.get();
        if ( sel == null )
        {
            throw new IllegalStateException( "VersionSchemeSelector is only available during EMB builds." );
        }

        return sel;
    }

    public static VersionScheme getTLVersionScheme( final String versionScheme )
    {
        return selector().getVersionScheme( versionScheme );
    }

    @Override
    public void executionFinished( final EMBManagementView managementView )
    {
        instance.remove();
    }

    @Override
    public void executionStarting( final EMBManagementView managementView )
    {
        instance.set( this );
        if ( library.getLogger().isDebugEnabled() )
        {
            library.getLogger().debug( "Version schemes available: "
                                           + StringUtils.join( schemes.keySet().iterator(), "\n\t" ) );
        }
    }

    public VersionScheme getCurrentVersionScheme()
    {
        final String key = (String) advisor.getRawAdvice( VersionScheme.VERSION_SCHEME_ADVICE );

        return getVersionScheme( key );
    }

    public VersionScheme getVersionScheme( final String key )
    {
        VersionScheme scheme = null;
        if ( key != null )
        {
            if ( schemes == null || !schemes.containsKey( key ) )
            {
                if ( library.getLogger().isDebugEnabled() )
                {
                    library.getLogger().debug( "Version-scheme map is missing or doesn't contain key: " + key
                                                   + ". Using default." );
                }
            }
            else
            {
                scheme = schemes.get( key );
            }
        }
        else if ( library.getLogger().isDebugEnabled() )
        {
            library.getLogger().debug( "No version scheme has been selected. Using default." );
        }

        return scheme == null ? defaultVersionScheme : scheme;
    }

}
