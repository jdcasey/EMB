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

package org.commonjava.emb.identity;

import java.net.URI;

public interface IdentityManager
{

    /**
     * Lookup the identity for use with the given realm. In legacy use cases, realm will be roughly equivalent to the
     * &lt;id/&gt; element value within a given &lt;server/&gt; definition, in the settings.xml file.
     * 
     * This method is provided to supply the legacy usage, which is realm-keyed credentials. The preferred mechanism
     * takes into account the target {@link URI} as well.
     * 
     * @throws IdentityException
     *             In cases where the identity provider cannot load properly.
     * @see IdentityManager#lookupIdentity(String, URI)
     */
    Identity lookupIdentity( String realm )
        throws IdentityException;

    /**
     * Lookup the identity for use with the given realm and target {@link URI}.
     * 
     * The realm is typically supplied by the server in use cases like HTTP authentication, with the target URI forming
     * the other part of the key for looking up identities.
     * 
     * <b>NOTE: To preserve backward compatibility, in cases where no exact matches are found, implementations MUST
     * favor matches on the realm over matches on the target URI.</b>
     * 
     * @throws IdentityException
     *             In cases where the identity provider cannot load properly.
     */
    Identity lookupIdentity( String realm, URI target )
        throws IdentityException;

}
