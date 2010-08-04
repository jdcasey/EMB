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

package org.apache.maven.experimental.artifact.resolver;

import org.apache.maven.project.ProjectDependenciesResolver;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentSelector;

@Service( XavenLibrary.class )
public class ExampleResolverLibrary
    extends AbstractXavenLibrary
{
    public ExampleResolverLibrary()
    {
        super( "example-artifact-resolver", "Xaven-Example-Artifact-Resolver",
               new MavenPomVersionProvider( "org.commonjava.xaven.examples", "example-artifact-resolver" ), "resolver",
               new ComponentSelector().setSelection( ProjectDependenciesResolver.class, "dummy" ) );
    }
}
