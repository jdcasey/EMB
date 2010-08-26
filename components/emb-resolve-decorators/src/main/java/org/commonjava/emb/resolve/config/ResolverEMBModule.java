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

package org.commonjava.emb.resolve.config;

import org.commonjava.maven.plexus.conf.EMBGuiceModule;
import org.commonjava.emb.resolve.event.EventDispatcher;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class ResolverEMBModule
    extends AbstractModule
    implements EMBGuiceModule
{

    @Override
    protected void configure()
    {
        bind( EventDispatcher.class ).annotatedWith( Names.named( "wagonManager" ) )
                                     .to( EventDispatcher.class )
                                     .in( Scopes.SINGLETON );
    }

}
