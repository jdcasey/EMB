package org.commonjava.xaven.conf;

import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.StringUtils.isBlank;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;
import static org.codehaus.plexus.util.StringUtils.join;

import org.apache.log4j.Logger;
import org.commonjava.xaven.conf.ext.ExtensionConfiguration;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationException;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

public final class XavenLibraries
{

    private static final String INFO_KEY_ID = "Id";

    private static final String INFO_KEY_NAME = "Name";

    private static final String INFO_KEY_VERSION = "Version";

    private static final String INFO_KEY_LOG_HANDLE = "Log-Handle";

    private static final String INFO_KEY_CONFIG_LOADER = "Configuration-Loader";

    private static final String XAVEN_INFO_PATH = "META-INF/xaven/extension.info.properties";

    private static final String XAVEN_COMPONENT_OVERRIDES_PATH = "META-INF/xaven/component-overrides.properties";

    private static final Logger logger = Logger.getLogger( XavenConfiguration.STANDARD_LOG_HANDLE_LOADER );

    private static Map<String, XavenLibrary> libraries;

    public static Map<String, XavenLibrary> loadLibraryInformation( final XavenConfiguration xavenConfig )
        throws IOException
    {
        if ( libraries != null )
        {
            return libraries;
        }

        libraries = new HashMap<String, XavenLibrary>();
        final List<String> extIdAndPath = new ArrayList<String>();

        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();

        final Enumeration<URL> resources = cloader.getResources( XAVEN_INFO_PATH );

        boolean foundOverlap = false;
        while ( resources.hasMoreElements() )
        {
            final URL resource = resources.nextElement();
            final String path = getJarPath( resource );

            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Loading extension info from: " + path );
            }

            InputStream stream = null;
            try
            {
                stream = resource.openStream();
                final Properties p = new Properties();
                p.load( stream );

                final String id = p.getProperty( INFO_KEY_ID );
                if ( isBlank( id ) )
                {
                    if ( logger.isDebugEnabled() )
                    {
                        logger.error( "Missing required value '" + INFO_KEY_ID + "' for library from: " + path );
                    }
                    continue;
                }

                extIdAndPath.add( id + ": " + path );
                if ( libraries.containsKey( id ) )
                {
                    if ( logger.isDebugEnabled() )
                    {
                        logger.error( "DUPLICATE LIBRARY ID FOUND: '" + id + "'. See below." );
                    }
                    foundOverlap = true;
                    continue;
                }

                final String name = p.getProperty( INFO_KEY_NAME, path );
                final String version = p.getProperty( INFO_KEY_VERSION, "UNKNOWN" );
                final String logHandle = p.getProperty( INFO_KEY_LOG_HANDLE, id );
                final String loaderCls = p.getProperty( INFO_KEY_CONFIG_LOADER );
                ExtensionConfiguration configuration = null;

                if ( isNotBlank( loaderCls ) )
                {
                    final ExtensionConfigurationLoader loader =
                        (ExtensionConfigurationLoader) cloader.loadClass( loaderCls ).newInstance();

                    configuration = loader.loadConfiguration( xavenConfig );
                }

                final XavenLibrary ext = new XavenLibrary( id, name, version, logHandle, configuration );
                libraries.put( id, ext );
            }
            catch ( final IOException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to read library info from: " + path, e );
                }
            }
            catch ( final InstantiationException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to load library configuration for: " + path, e );
                }
            }
            catch ( final IllegalAccessException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to load library configuration for: " + path, e );
                }
            }
            catch ( final ClassNotFoundException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to load library configuration for: " + path, e );
                }
            }
            catch ( final ExtensionConfigurationException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to load library configuration for: " + path, e );
                }
            }
            finally
            {
                close( stream );
            }
        }

        if ( foundOverlap && logger.isDebugEnabled() )
        {
            logger.error( "The following library information paths were encountered:\n\n"
                + join( extIdAndPath.iterator(), "\n\t" ) );
        }

        return libraries;
    }

    private static String getJarPath( final URL resource )
    {
        String path = resource.getPath();
        final int idx = path.indexOf( '!' );
        if ( idx > 0 )
        {
            path = path.substring( 0, idx );
        }

        return new File( path ).getAbsolutePath();
    }

    public static Properties getComponentOverrides()
        throws IOException
    {
        final Properties overrides = new Properties();

        final Map<String, String> keyOrigins = new HashMap<String, String>();

        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();

        final Enumeration<URL> resources = cloader.getResources( XAVEN_COMPONENT_OVERRIDES_PATH );
        while ( resources.hasMoreElements() )
        {
            final URL resource = resources.nextElement();
            final String path = getJarPath( resource );

            InputStream stream = null;
            try
            {
                stream = resource.openStream();
                final Properties p = new Properties();
                p.load( stream );

                for ( final Object k : p.keySet() )
                {
                    final String key = (String) k;

                    if ( logger.isDebugEnabled() && overrides.containsKey( key ) )
                    {
                        logger.debug( "REPLACING component override: '" + key + "'\nSupplied by: "
                            + keyOrigins.get( key ) + "\nReplaced by: " + path );
                    }

                    overrides.setProperty( key, p.getProperty( key ) );
                    keyOrigins.put( key, path );
                }
            }
            catch ( final IOException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to read component overrides from: " + path, e );
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
