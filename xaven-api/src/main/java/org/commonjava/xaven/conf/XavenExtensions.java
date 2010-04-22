package org.commonjava.xaven.conf;

import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import org.apache.log4j.Logger;
import org.commonjava.xaven.conf.ext.ExtensionConfiguration;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationException;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

public final class XavenExtensions
{

    private static final String INFO_KEY_NAME = "Name";

    private static final String INFO_KEY_CONFIG_LOADER = "Configuration-Loader";

    private static final String XAVEN_INFO_PATH = "META-INF/xaven/extension.info.properties";

    private static final String XAVEN_COMPONENT_OVERRIDES_PATH = "META-INF/xaven/component-overrides.properties";

    private static final Logger logger = Logger.getLogger( XavenExtensions.class );

    public static Set<String> getLoadedExtensions( final boolean debug )
        throws IOException
    {
        final Set<String> ext = new LinkedHashSet<String>();

        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();

        final Enumeration<URL> resources = cloader.getResources( XAVEN_INFO_PATH );
        while ( resources.hasMoreElements() )
        {
            final URL resource = resources.nextElement();
            final String path = resource.getPath();

            InputStream stream = null;
            try
            {
                stream = resource.openStream();
                final Properties p = new Properties();
                p.load( stream );

                final String name = p.getProperty( INFO_KEY_NAME, path );
                ext.add( name );
            }
            catch ( final IOException e )
            {
                if ( debug )
                {
                    logger.error( "Failed to read extension info from: " + path, e );
                }
            }
            finally
            {
                close( stream );
            }
        }

        return ext;
    }

    public static Map<String, ? extends ExtensionConfiguration> loadExtensionConfigurations(
                                                                                             final XavenConfiguration xavenConfig,
                                                                                             final boolean debug )
        throws IOException
    {
        final Map<String, ExtensionConfiguration> configs = new HashMap<String, ExtensionConfiguration>();

        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();

        final Enumeration<URL> resources = cloader.getResources( XAVEN_INFO_PATH );
        while ( resources.hasMoreElements() )
        {
            final URL resource = resources.nextElement();
            final String path = resource.getPath();
            String name = path;

            InputStream stream = null;
            try
            {
                stream = resource.openStream();
                final Properties p = new Properties();
                p.load( stream );

                name = p.getProperty( INFO_KEY_NAME, path );
                final String cls = p.getProperty( INFO_KEY_CONFIG_LOADER );

                if ( isNotBlank( cls ) )
                {
                    final ExtensionConfigurationLoader loader =
                        (ExtensionConfigurationLoader) cloader.loadClass( cls ).newInstance();

                    configs.put( loader.getExtensionConfigurationClass().getName(),
                                 loader.loadConfiguration( xavenConfig ) );
                }
            }
            catch ( final IOException e )
            {
                if ( debug )
                {
                    logger.error( "Failed to read extension info from: " + path, e );
                }
            }
            catch ( final InstantiationException e )
            {
                if ( debug )
                {
                    logger.error( "Failed to load extension configuration for: " + name, e );
                }
            }
            catch ( final IllegalAccessException e )
            {
                if ( debug )
                {
                    logger.error( "Failed to load extension configuration for: " + name, e );
                }
            }
            catch ( final ClassNotFoundException e )
            {
                if ( debug )
                {
                    logger.error( "Failed to load extension configuration for: " + name, e );
                }
            }
            catch ( final ExtensionConfigurationException e )
            {
                if ( debug )
                {
                    logger.error( "Failed to load extension configuration for: " + name, e );
                }
            }
            finally
            {
                close( stream );
            }
        }

        return configs;
    }

    public static Properties getComponentOverrides( final boolean debug )
        throws IOException
    {
        final Properties overrides = new Properties();

        final Map<String, String> keyOrigins = new HashMap<String, String>();

        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();

        final Enumeration<URL> resources = cloader.getResources( XAVEN_COMPONENT_OVERRIDES_PATH );
        while ( resources.hasMoreElements() )
        {
            final URL resource = resources.nextElement();
            final String path = resource.getPath();

            InputStream stream = null;
            try
            {
                stream = resource.openStream();
                final Properties p = new Properties();
                p.load( stream );

                for ( final Object k : p.keySet() )
                {
                    final String key = (String) k;

                    if ( debug && overrides.containsKey( key ) )
                    {
                        logger.warn( "REPLACING component override: '" + key + "'\nSupplied by: "
                            + keyOrigins.get( key ) + "\nReplaced by: " + path );
                    }

                    overrides.setProperty( key, p.getProperty( key ) );
                    keyOrigins.put( key, path );
                }
            }
            catch ( final IOException e )
            {
                if ( debug )
                {
                    logger.error( "Failed to read component overrides from: " + path, e );
                }
            }
            finally
            {
                close( stream );
            }
        }

        return overrides;
    }

}
