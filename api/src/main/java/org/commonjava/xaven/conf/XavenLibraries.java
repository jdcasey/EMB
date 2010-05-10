package org.commonjava.xaven.conf;

import org.apache.log4j.Logger;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

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

    private static final Logger logger = Logger.getLogger( XavenConfiguration.STANDARD_LOG_HANDLE_LOADER );

    private static Map<String, XavenLibrary> libraries;

    public static Collection<XavenLibrary> loadLibraries( final XavenConfiguration xavenConfig )
        throws IOException
    {
        if ( libraries != null )
        {
            xavenConfig.withLibraries( libraries );
            return libraries.values();
        }

        libraries = new HashMap<String, XavenLibrary>();
        final ServiceLoader<XavenLibrary> loader = ServiceLoader.load( XavenLibrary.class );
        for ( final XavenLibrary library : loader )
        {
            try
            {
                library.loadConfiguration( xavenConfig );
            }
            catch ( final ExtensionConfigurationException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to load library configuration for: '" + library.getId() + "'. Reason: "
                        + e.getMessage(), e );
                }
            }

            libraries.put( library.getId(), library );
            xavenConfig.withLibrary( library );
        }

        return libraries.values();
    }

}
