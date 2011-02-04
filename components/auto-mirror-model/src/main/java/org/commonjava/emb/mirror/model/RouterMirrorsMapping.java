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
package org.commonjava.emb.mirror.model;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedHashMap;
import java.util.Map;

public class RouterMirrorsMapping
{

    @SerializedName( "rows" )
    private final Map<String, RouterMirrors> mirrorsByUrl = new LinkedHashMap<String, RouterMirrors>();

    public RouterMirrorsMapping addMirrorCollection( final String url, final RouterMirrors collection )
    {
        mirrorsByUrl.put( url, collection );
        return this;
    }

    public RouterMirror getSelectedMirror( final String url )
    {
        final RouterMirrors mirrors = getMirrorsFor( url );
        if ( mirrors != null )
        {
            return mirrors.getSelectedMirror();
        }

        return null;
    }

    public RouterMirror getHighestPriorityMirror( final String url )
    {
        final RouterMirrors mirrors = getMirrorsFor( url );
        if ( mirrors != null )
        {
            return mirrors.getHighestPriorityMirror();
        }

        return null;
    }

    public RouterMirror getWeightedRandomSuggestion( final String url )
    {
        final RouterMirrors mirrors = getMirrorsFor( url );
        if ( mirrors != null )
        {
            return mirrors.getWeightedRandomSuggestion();
        }

        return null;
    }

    public synchronized RouterMirrorsMapping clearSelected()
    {
        for ( final RouterMirrors m : mirrorsByUrl.values() )
        {
            m.clearSelected();
        }

        return this;
    }

    public synchronized RouterMirrorsMapping addMirror( final String url, final RouterMirror mirror )
    {
        RouterMirrors collection = getMirrorsFor( url );
        if ( collection == null )
        {
            collection = new RouterMirrors();
            mirrorsByUrl.put( url, collection );
        }

        collection.addMirror( mirror );
        return this;
    }

    public RouterMirrors getMirrorsFor( final String url )
    {
        RouterMirrors mirrors = mirrorsByUrl.get( url );
        if ( mirrors == null )
        {
            if ( url.endsWith( "/" ) )
            {
                mirrors = mirrorsByUrl.get( url.substring( 0, url.length() - 1 ) );
            }
            else
            {
                mirrors = mirrorsByUrl.get( url + "/" );
            }
        }

        return mirrors;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( mirrorsByUrl == null ) ? 0 : mirrorsByUrl.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final RouterMirrorsMapping other = (RouterMirrorsMapping) obj;
        if ( mirrorsByUrl == null )
        {
            if ( other.mirrorsByUrl != null )
            {
                return false;
            }
        }
        else if ( !mirrorsByUrl.equals( other.mirrorsByUrl ) )
        {
            return false;
        }
        return true;
    }

    public boolean containsMirrorOf( final String url )
    {
        return getMirrorsFor( url ) != null;
    }

}
