/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.component.vscheme;

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
        throws EMBArtifactVersionException
    {
        if ( SchemeAwareVersionRange.isRange( version ) )
        {
            try
            {
                return SchemeAwareVersionRange.createFromVersionSpec( version, this );
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                throw new EMBArtifactVersionException( "Failed to create range from specification: %s. Reason: %s",
                                                         e, version, e.getMessage() );
            }
        }
        else
        {
            return SchemeAwareVersionRange.createFromVersion( version, this );
        }
    }

}
