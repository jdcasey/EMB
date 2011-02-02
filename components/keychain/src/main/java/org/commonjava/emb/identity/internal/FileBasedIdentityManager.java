/*
 *  Copyright (C) 2011 John Casey.
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

package org.commonjava.emb.identity.internal;

import org.commonjava.emb.identity.Identity;
import org.commonjava.emb.identity.IdentityException;
import org.commonjava.emb.identity.IdentityManager;

import java.net.URI;

public class FileBasedIdentityManager
    implements IdentityManager
{

    private static final ThreadLocal<IdentityMap> identityMapTL = new InheritableThreadLocal<IdentityMap>();

    @Override
    public Identity lookupIdentity( final String realm )
        throws IdentityException
    {
        // TODO Implement FileBasedIdentityManager.lookupIdentity
        throw new UnsupportedOperationException( "Not Implemented." );
    }

    @Override
    public Identity lookupIdentity( final String realm, final URI target )
        throws IdentityException
    {
        // TODO Implement FileBasedIdentityManager.lookupIdentity
        throw new UnsupportedOperationException( "Not Implemented." );
    }

}
