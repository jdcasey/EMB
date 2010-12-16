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

package org.commonjava.emb.version.autobox.lib;

import org.commonjava.emb.conf.ext.ExtensionConfiguration;

public class AutoboxingConfig
    implements ExtensionConfiguration
{

    private final boolean autoBox;

    private final String[] qualifierOrder;

    private final String rebuildQualifier;

    public AutoboxingConfig( final boolean autoBox, final String rebuildQualifier, final String... qualifierOrder )
    {
        this.autoBox = autoBox;
        this.rebuildQualifier = rebuildQualifier;
        this.qualifierOrder = qualifierOrder;
    }

    public boolean isAutoBox()
    {
        return autoBox;
    }

    public String[] getQualifierOrder()
    {
        // make the ordering immutable.
        final String[] result = new String[qualifierOrder.length];
        System.arraycopy( qualifierOrder, 0, result, 0, qualifierOrder.length );

        return result;
    }

    public String getRebuildQualifier()
    {
        return rebuildQualifier;
    }

}
