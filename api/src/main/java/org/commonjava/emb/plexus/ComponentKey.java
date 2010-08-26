package org.commonjava.emb.plexus;

import static org.codehaus.plexus.util.StringUtils.isBlank;

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

public class ComponentKey
{

    private final String role;

    private final String hint;

    public ComponentKey( final Class<?> role, final String hint )
    {
        this.role = role.getName();
        this.hint = hint;
    }

    public ComponentKey( final String role, final String hint )
    {
        this.role = role;
        this.hint = hint;
    }

    public ComponentKey( final Class<?> role )
    {
        this.role = role.getName();
        hint = null;
    }

    public ComponentKey( final String role )
    {
        this.role = role;
        hint = null;
    }

    public String getRole()
    {
        return role;
    }

    public String getHint()
    {
        return hint;
    }

    public String key()
    {
        return role + ( isBlank( hint ) ? "" : "#" + hint );
    }

    @Override
    public String toString()
    {
        return key();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( hint == null ) ? 0 : hint.hashCode() );
        result = prime * result + ( ( role == null ) ? 0 : role.hashCode() );
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
        final ComponentKey other = (ComponentKey) obj;
        if ( hint == null )
        {
            if ( other.hint != null )
            {
                return false;
            }
        }
        else if ( !hint.equals( other.hint ) )
        {
            return false;
        }
        if ( role == null )
        {
            if ( other.role != null )
            {
                return false;
            }
        }
        else if ( !role.equals( other.role ) )
        {
            return false;
        }
        return true;
    }

}
