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

package org.commonjava.xaven.support.mavenscm;

import org.apache.maven.scm.manager.ScmManager;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;
import org.commonjava.xaven.conf.XavenLibrary;
import org.commonjava.xaven.plexus.ComponentKey;

@Service( XavenLibrary.class )
public class MavenSCMExportLibrary
    extends AbstractXavenLibrary
{

    public MavenSCMExportLibrary()
    {
        super( "maven-scm-export", "MavenSCM-Service-Exporter",
               new MavenPomVersionProvider( "org.commonjava.xaven.export", "maven-scm-export" ) );

        withExportedComponent( new ComponentKey( ScmManager.class ) );
    }

}
