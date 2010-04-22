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

package org.commonjava.xaven.nexus.conf;

import static org.codehaus.plexus.util.IOUtil.close;

import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.conf.ext.ExtensionConfiguration;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationException;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AutoNXConfigLoader
    implements ExtensionConfigurationLoader
{

    private static final String AUTONX_CONFIG_FILENAME = "autonx.properties";

    private static final String KEY_NEXUS_URL = "nexus-url";

    private static final String KEY_MIRROR_ID = "mirror-id";

    public Class<? extends ExtensionConfiguration> getExtensionConfigurationClass()
    {
        return AutoNXConfiguration.class;
    }

    public ExtensionConfiguration loadConfiguration( final XavenConfiguration xavenConfig )
        throws ExtensionConfigurationException
    {
        final File configFile = new File( xavenConfig.getConfigurationDirectory(), AUTONX_CONFIG_FILENAME );
        final AutoNXConfiguration config = new AutoNXConfiguration();

        if ( configFile.exists() )
        {
            FileInputStream stream = null;
            try
            {
                stream = new FileInputStream( configFile );
                final Properties p = new Properties();
                p.load( stream );

                config.withMirrorId( p.getProperty( KEY_MIRROR_ID ) ).withNexusUrl( p.getProperty( KEY_NEXUS_URL ) );
            }
            catch ( final IOException e )
            {
                throw new ExtensionConfigurationException( "Failed to read properties: '{0}' from: {1}\nReason: {2}",
                                                           e, AUTONX_CONFIG_FILENAME,
                                                           xavenConfig.getConfigurationDirectory(), e.getMessage() );
            }
            finally
            {
                close( stream );
            }
        }

        return config;
    }

}
