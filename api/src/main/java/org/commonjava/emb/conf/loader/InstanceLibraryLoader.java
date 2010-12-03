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

import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class InstanceLibraryLoader
    implements EMBLibraryLoader
{

    private final List<EMBLibrary> libraries;

    public InstanceLibraryLoader( final EMBLibrary... libraries )
    {
        this.libraries =
            libraries == null ? new ArrayList<EMBLibrary>() : new ArrayList<EMBLibrary>( Arrays.asList( libraries ) );
    }

    public InstanceLibraryLoader( final List<EMBLibrary> libraries )
    {
        this.libraries = libraries;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.commonjava.emb.conf.loader.EMBLibraryLoader#loadLibraries(org.commonjava.emb.conf.EMBConfiguration)
     */
    @Override
    public Collection<EMBLibrary> loadLibraries( final EMBConfiguration embConfig )
        throws IOException
    {
        return libraries;
    }

}
