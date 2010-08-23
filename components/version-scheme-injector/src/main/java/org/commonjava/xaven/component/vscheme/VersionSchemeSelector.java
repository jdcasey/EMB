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
import org.commonjava.xaven.XavenAdvisor;
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

    @Requirement( hint = "default" )
    private VersionScheme defaultVersionScheme;

    @Requirement
    private XavenAdvisor advisor;

    public static VersionScheme getCurrentVersionScheme()
    {
        final VersionSchemeSelector sel = instance.get();
        if ( sel == null )
        {
            throw new IllegalStateException( "VersionSchemeSelector is only available during Xaven builds." );
        }

        return sel.getVersionScheme();
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
    }

    public VersionScheme getVersionScheme()
    {
        final String key = (String) advisor.getRawAdvice( VersionScheme.VERSION_SCHEME_ADVICE );

        final VersionScheme scheme = schemes == null ? null : schemes.get( key );
        return scheme == null ? defaultVersionScheme : scheme;
    }

}
