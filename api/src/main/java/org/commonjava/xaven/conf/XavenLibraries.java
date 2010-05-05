package org.commonjava.xaven.conf;

import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.StringUtils.join;

import org.apache.log4j.Logger;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String XAVEN_LIBRARY_SERVICE_PATH = "META-INF/xaven/libraries.conf";

    private static final Logger logger = Logger.getLogger( XavenConfiguration.STANDARD_LOG_HANDLE_LOADER );

    private static Map<String, XavenLibrary> libraries;

    public static void loadLibraryInformation( final XavenConfiguration xavenConfig )
        throws IOException
    {
        if ( libraries != null )
        {
            xavenConfig.withLibraries( libraries );
            return;
        }

        libraries = new HashMap<String, XavenLibrary>();
        final List<String> overlappingLabels = new ArrayList<String>();

        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();

        final Enumeration<URL> resources = cloader.getResources( XAVEN_LIBRARY_SERVICE_PATH );

        boolean foundOverlap = false;
        while ( resources.hasMoreElements() )
        {
            final URL resource = resources.nextElement();
            final String path = getJarPath( resource );

            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Loading extension info from: " + path );
            }

            BufferedReader reader = null;
            try
            {
                reader = new BufferedReader( new InputStreamReader( resource.openStream() ) );
                String line = null;
                while ( ( line = reader.readLine() ) != null )
                {
                    line = line.trim();
                    try
                    {
                        final XavenLibrary library = (XavenLibrary) cloader.loadClass( line ).newInstance();
                        if ( libraries.containsKey( library.getId() ) )
                        {
                            foundOverlap = true;
                            overlappingLabels.add( library.getLabel() );
                        }

                        library.loadConfiguration( xavenConfig );
                        libraries.put( library.getId(), library );
                        xavenConfig.withLibrary( library );
                    }
                    catch ( final InstantiationException e )
                    {
                        if ( logger.isDebugEnabled() )
                        {
                            logger.debug( "Failed to load library configuration for: " + line + ", from resource: "
                                + path, e );
                        }
                    }
                    catch ( final IllegalAccessException e )
                    {
                        if ( logger.isDebugEnabled() )
                        {
                            logger.debug( "Failed to load library configuration for: " + line + ", from resource: "
                                + path, e );
                        }
                    }
                    catch ( final ClassNotFoundException e )
                    {
                        if ( logger.isDebugEnabled() )
                        {
                            logger.debug( "Failed to load library configuration for: " + line + ", from resource: "
                                + path, e );
                        }
                    }
                    catch ( final ExtensionConfigurationException e )
                    {
                        if ( logger.isDebugEnabled() )
                        {
                            logger.debug( "Failed to load library configuration for: " + line + ", from resource: "
                                + path, e );
                        }
                    }
                }
            }
            catch ( final IOException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to read library info from: " + path, e );
                }
            }
            finally
            {
                close( reader );
            }
        }

        if ( foundOverlap && logger.isDebugEnabled() )
        {
            logger.error( "The following overlapping library information paths were encountered:\n\n"
                + join( overlappingLabels.iterator(), "\n\t" ) );
        }
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

}
