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

import org.apache.commons.io.IOUtils;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfigurationException;
import org.commonjava.emb.conf.ext.ExtensionConfigurationLoader;
import org.commonjava.emb.version.autobox.qual.DefaultQualifiers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AutoboxingConfigReader
    implements ExtensionConfigurationLoader
{

    public static final String FILENAME = "abx.properties";

    private static final String KEY_DO_AUTOBOX = "autobox";

    private static final String KEY_REBUILD_QUALIFIER = "qualifier.rebuild";

    private static final String KEY_QUALIFIER_ORDER = "qualifier.order";

    private final AutoboxingConfig config;

    public AutoboxingConfigReader()
    {
        config = null;
    }

    public AutoboxingConfigReader( final AutoboxingConfig config )
    {
        this.config = config;
    }

    @Override
    public ExtensionConfiguration loadConfiguration( final EMBConfiguration embConfig )
        throws ExtensionConfigurationException
    {
        if ( config != null )
        {
            return config;
        }

        final File f = new File( embConfig.getConfigurationDirectory(), FILENAME );

        final Properties p = new Properties();
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( f );
            p.load( fis );

            final boolean autobox = Boolean.parseBoolean( p.getProperty( KEY_DO_AUTOBOX, "false" ) );
            final String rebuild = p.getProperty( KEY_REBUILD_QUALIFIER, DefaultQualifiers.INSTANCE.rebuildIndicator() );
            final String orderStr = p.getProperty( KEY_QUALIFIER_ORDER );

            final String[] order =
                orderStr != null ? orderStr.split( "\\s*,\\s*" ) : DefaultQualifiers.INSTANCE.order();

            return new AutoboxingConfig( autobox, rebuild, order );
        }
        catch ( final IOException e )
        {
            throw new ExtensionConfigurationException(
                                                       "Failed to read configuration from: %s\nConfiguration directory: %s.\nReason: %s",
                                                       e, FILENAME, embConfig.getConfigurationDirectory(),
                                                       e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( fis );
        }
    }
}
