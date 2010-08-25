/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonjava.xaven.component.vscheme;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.commonjava.xaven.XavenAdvisor;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.conf.mgmt.LoadOnFinish;
import org.commonjava.xaven.conf.mgmt.LoadOnStart;
import org.commonjava.xaven.conf.mgmt.XavenManagementView;

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
    private XavenLibrary library;

    @Requirement
    private XavenAdvisor advisor;

    public static VersionScheme getTLCurrentVersionScheme()
    {
        return selector().getCurrentVersionScheme();
    }

    private static VersionSchemeSelector selector()
    {
        final VersionSchemeSelector sel = instance.get();
        if ( sel == null )
        {
            throw new IllegalStateException( "VersionSchemeSelector is only available during Xaven builds." );
        }

        return sel;
    }

    public static VersionScheme getTLVersionScheme( final String versionScheme )
    {
        return selector().getVersionScheme( versionScheme );
    }

    @Override
    public void executionFinished( final XavenManagementView managementView )
    {
        instance.remove();
    }

    @Override
    public void executionStarting( final XavenManagementView managementView )
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
