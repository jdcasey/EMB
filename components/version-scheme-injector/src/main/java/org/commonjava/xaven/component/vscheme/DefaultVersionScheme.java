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

import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.codehaus.plexus.component.annotations.Component;

import java.util.Comparator;

@Component( role = VersionScheme.class, hint = VersionScheme.DEFAULT_KEY )
public class DefaultVersionScheme
    implements VersionScheme
{

    @Override
    public VersionComparison getComparableVersion( final String version )
    {
        return new VersionComparison( version );
    }

    @Override
    public Comparator<String> getVersionStringComparator()
    {
        return new VersionStringComparator( this );
    }

    @Override
    public SchemeAwareVersionRange createRange( final String version )
        throws XavenArtifactVersionException
    {
        if ( SchemeAwareVersionRange.isRange( version ) )
        {
            try
            {
                return SchemeAwareVersionRange.createFromVersionSpec( version, this );
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                throw new XavenArtifactVersionException( "Failed to create range from specification: %s. Reason: %s",
                                                         e, version, e.getMessage() );
            }
        }
        else
        {
            return SchemeAwareVersionRange.createFromVersion( version, this );
        }
    }

}
