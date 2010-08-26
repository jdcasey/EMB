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

package org.commonjava.emb.event.resolver;

import org.apache.maven.ProjectDependenciesResolver;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.apache.maven.repository.RepositorySystem;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.plexus.ComponentSelector;

@Service( EMBLibrary.class )
public class ResolverEventAPILibrary
    extends AbstractEMBLibrary
{
    public ResolverEventAPILibrary()
    {
        super( "resolver-events", "EMB-Resolver-Events", new MavenPomVersionProvider( "org.commonjava.emb.event",
                                                                                        "emb-m3-resolver-events" ),
               "events", new ComponentSelector().setSelection( PluginDependenciesResolver.class, "emb-events" )
                                                .setSelection( ProjectDependenciesResolver.class, "emb-events" )
                                                .setSelection( RepositorySystem.class, "emb-events" ) );
    }
}
