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

package org.commonjava.emb.plexus;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class VirtualInstance<T>
    implements Provider<T>
{

    @Inject
    private Injector injector;

    private T instance;

    private final Class<T> virtualClass;

    public VirtualInstance( final Class<T> virtualClass )
    {
        this.virtualClass = virtualClass;
    }

    public void setInstance( final T instance )
    {
        this.instance = instance;
    }

    public Class<T> getVirtualClass()
    {
        return virtualClass;
    }

    public T getRawInstance()
    {
        return instance;
    }

    @Override
    public T get()
    {
        if ( injector != null && instance != null )
        {
            injector.injectMembers( instance );
        }

        return instance;
    }

}
