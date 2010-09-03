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

package org.commonjava.emb.components.aether;

import java.util.Collection;

public class VersionFindException
    extends AetherWrapperException
{

    private static final long serialVersionUID = 1L;

    public VersionFindException( final String message, final Collection<Throwable> causes, final Object... params )
    {
        super( message, causes, params );
    }

    public VersionFindException( final String message, final Collection<Throwable> causes )
    {
        super( message, causes );
    }

    public VersionFindException( final String message, final Object... params )
    {
        super( message, params );
    }

    public VersionFindException( final String message, final Throwable cause, final Object... params )
    {
        super( message, cause, params );
    }

    public VersionFindException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public VersionFindException( final String message )
    {
        super( message );
    }

}
