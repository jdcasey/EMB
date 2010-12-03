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

package org.commonjava.emb.nexus.conf;

import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfigurationException;
import org.commonjava.emb.conf.ext.ExtensionConfigurationLoader;

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

    private static final String KEY_NEXUS_USER = "nexus-user";

    private static final String KEY_NEXUS_PASSWORD = "nexus-password";

    public Class<? extends ExtensionConfiguration> getExtensionConfigurationClass()
    {
        return AutoNXConfiguration.class;
    }

    public ExtensionConfiguration loadConfiguration( final EMBConfiguration embConfig )
        throws ExtensionConfigurationException
    {
        final File configFile = new File( embConfig.getConfigurationDirectory(), AUTONX_CONFIG_FILENAME );
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

                final String user = p.getProperty( KEY_NEXUS_USER );
                final String pass = p.getProperty( KEY_NEXUS_PASSWORD );

                if ( isNotBlank( user ) && isNotBlank( pass ) )
                {
                    config.withNexusCredentials( user, pass );
                }
            }
            catch ( final IOException e )
            {
                throw new ExtensionConfigurationException( "Failed to read properties: '{0}' from: {1}\nReason: {2}",
                                                           e, AUTONX_CONFIG_FILENAME,
                                                           embConfig.getConfigurationDirectory(), e.getMessage() );
            }
            finally
            {
                close( stream );
            }
        }

        return config;
    }

}
