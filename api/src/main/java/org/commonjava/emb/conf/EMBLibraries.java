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

package org.commonjava.emb.conf;

import org.apache.log4j.Logger;
import org.commonjava.emb.conf.ext.ExtensionConfigurationException;
import org.commonjava.emb.conf.loader.EMBLibraryLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EMBLibraries
{

    private static final Logger logger = Logger.getLogger( EMBConfiguration.STANDARD_LOG_HANDLE_LOADER );

    private static Set<EMBLibrary> libraries;

    public static Collection<EMBLibrary> loadLibraries( final EMBConfiguration embConfig,
                                                        final List<EMBLibraryLoader> loaders )
        throws IOException
    {
        if ( libraries != null )
        {
            return libraries;
        }

        libraries = new LinkedHashSet<EMBLibrary>();
        for ( final EMBLibraryLoader loader : loaders )
        {
            libraries.addAll( loader.loadLibraries( embConfig ) );
        }

        for ( final EMBLibrary library : libraries )
        {
            try
            {
                library.loadConfiguration( embConfig );
            }
            catch ( final ExtensionConfigurationException e )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Failed to load library configuration for: '" + library.getId() + "'. Reason: "
                                                  + e.getMessage(), e );
                }
            }
        }

        libraries = Collections.unmodifiableSet( libraries );
        return libraries;
    }

}
