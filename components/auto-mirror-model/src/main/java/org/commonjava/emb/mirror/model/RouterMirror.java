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

public class RouterMirror
    implements Comparable<RouterMirror>
{

    private final String id;

    private final String url;

    private final int weight;

    private final boolean enabled;

    RouterMirror()
    {
        id = null;
        url = null;
        weight = 0;
        enabled = false;
    }

    public RouterMirror( final String id, final String url, final int weight, final boolean enabled )
    {
        this.id = id;
        this.url = url;
        this.weight = weight;
        this.enabled = enabled;
    }

    public String getId()
    {
        return id;
    }

    public String getUrl()
    {
        return url;
    }

    public int getWeight()
    {
        return weight;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public int compareTo( final RouterMirror o )
    {
        return o.weight - weight;
    }

    @Override
    public String toString()
    {
        return "mirror [id: " + id + ", weight: " + weight + ", url: " + url + ", enabled: " + enabled + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( url == null ) ? 0 : url.hashCode() );
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
        final RouterMirror other = (RouterMirror) obj;
        if ( url == null )
        {
            if ( other.url != null )
            {
                return false;
            }
        }
        else if ( !url.equals( other.url ) )
        {
            return false;
        }
        return true;
    }

}
