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

package org.commonjava.emb.conf.loader;

import org.apache.log4j.Logger;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.ServiceLoader;

public class ServiceLibraryLoader
    implements EMBLibraryLoader
{

    @SuppressWarnings( "unused" )
    private static final Logger logger = Logger.getLogger( EMBConfiguration.STANDARD_LOG_HANDLE_LOADER );

    public Collection<EMBLibrary> loadLibraries( final EMBConfiguration embConfig )
        throws IOException
    {
        final LinkedHashSet<EMBLibrary> libraries = new LinkedHashSet<EMBLibrary>();

        final ServiceLoader<EMBLibrary> loader = ServiceLoader.load( EMBLibrary.class );
        for ( final EMBLibrary library : loader )
        {
            libraries.add( library );
        }

        return libraries;
    }

}
