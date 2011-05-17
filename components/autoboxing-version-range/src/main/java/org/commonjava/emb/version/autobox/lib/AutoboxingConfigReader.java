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

package org.commonjava.emb.version.autobox.lib;

import org.apache.commons.io.IOUtils;
import org.apache.maven.mae.conf.MAEConfiguration;
import org.apache.maven.mae.conf.ext.ExtensionConfiguration;
import org.apache.maven.mae.conf.ext.ExtensionConfigurationException;
import org.apache.maven.mae.conf.ext.ExtensionConfigurationLoader;
import org.commonjava.emb.version.autobox.qual.DefaultQualifiers;
import org.commonjava.emb.version.autobox.qual.Qualifiers;

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
    public ExtensionConfiguration loadConfiguration( final MAEConfiguration embConfig )
        throws ExtensionConfigurationException
    {
        if ( config != null )
        {
            return config;
        }

        final File f = new File( embConfig.getConfigurationDirectory(), FILENAME );
        if ( f.exists() && f.isFile() && f.canRead() )
        {
            final Properties p = new Properties();
            FileInputStream fis = null;
            try
            {
                fis = new FileInputStream( f );
                p.load( fis );

                final boolean autobox = Boolean.parseBoolean( p.getProperty( KEY_DO_AUTOBOX, "false" ) );
                final String rebuild =
                    p.getProperty( KEY_REBUILD_QUALIFIER, DefaultQualifiers.INSTANCE.rebuildIndicator() );
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

        final Qualifiers q = DefaultQualifiers.INSTANCE;
        return new AutoboxingConfig( false, q.rebuildIndicator(), q.order() );
    }
}
