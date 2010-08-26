package org.commonjava.emb.conf;

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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class MavenPomVersionProvider
    implements VersionProvider
{

    private static final Logger logger = Logger.getLogger( EMBConfiguration.STANDARD_LOG_HANDLE_LOADER );

    private final String groupId;

    private final String artifactId;

    private String version;

    public MavenPomVersionProvider( final String groupId, final String artifactId )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public final synchronized String getVersion()
    {
        if ( version == null )
        {
            final String path = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
            final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( path );

            if ( stream != null )
            {
                final Properties p = new Properties();
                try
                {
                    p.load( stream );
                    version = p.getProperty( "version" );
                }
                catch ( final IOException e )
                {
                    if ( logger.isDebugEnabled() )
                    {
                        logger.debug( "Failed to load version for: " + groupId + ":" + artifactId );
                    }
                }
            }

            if ( version == null )
            {
                version = "-UNKNOWN-";
            }
        }

        return version;
    }

}
