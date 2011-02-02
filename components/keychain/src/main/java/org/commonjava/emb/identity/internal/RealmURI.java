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

import java.net.URI;
import java.net.URISyntaxException;

final class RealmURI
{

    private static final URI ANY_TARGET;

    private static final String ANY_REALM = "*";

    static
    {
        try
        {
            ANY_TARGET = new URI( "all:*" );
        }
        catch ( final URISyntaxException e )
        {
            throw new IllegalStateException( "Cannot initialize URI wildcard for identity management." );
        }
    }

    private final String realm;

    private final URI target;

    RealmURI( final String realm, final URI target )
    {
        this.realm = realm;
        this.target = target;
    }

    final URI getTargetURI()
    {
        return target;
    }

    final String getRealm()
    {
        return realm;
    }

    final int scoreMatch( final String realm, final URI target )
    {
        int score = 0;
        if ( !ANY_REALM.equals( realm ) && this.realm.equals( realm ) )
        {
            score++;
        }
        
        if ( !ANY_REALM)
    }
}
