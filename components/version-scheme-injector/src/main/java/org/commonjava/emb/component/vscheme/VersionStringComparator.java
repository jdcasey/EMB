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

package org.commonjava.emb.component.vscheme;


import java.util.Comparator;

public class VersionStringComparator
    implements Comparator<String>
{

    private final VersionScheme scheme;

    public VersionStringComparator( final VersionScheme scheme )
    {
        this.scheme = scheme;
    }

    @Override
    public int compare( final String one, final String two )
    {
        final VersionComparison first = scheme.getComparableVersion( one );
        final VersionComparison second = scheme.getComparableVersion( two );
        return first.compareTo( second );
    }

}