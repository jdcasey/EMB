package org.commonjava.emb.plexus;

import java.util.HashMap;
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

public class InstanceRegistry
{

    private final Map<ComponentKey<?>, Object> instances = new HashMap<ComponentKey<?>, Object>();

    public InstanceRegistry()
    {
    }

    public InstanceRegistry( final InstanceRegistry... delegates )
    {
        if ( delegates != null && delegates.length > 0 )
        {
            for ( final InstanceRegistry delegate : delegates )
            {
                overrideMerge( delegate );
            }
        }
    }

    public boolean has( final ComponentKey<?> key )
    {
        if ( key == null )
        {
            return false;
        }

        return instances.containsKey( key );
    }

    public <T> boolean has( final Class<T> role, final String hint )
    {
        return has( new ComponentKey<T>( role, hint ) );
    }

    public <T> T get( final ComponentKey<T> key )
    {
        return key.castValue( instances.get( key ) );
    }

    public <T> T get( final Class<T> role, final String hint )
    {
        return get( new ComponentKey<T>( role, hint ) );
    }

    public <T> InstanceRegistry add( final ComponentKey<T> key, final T instance )
    {
        instances.put( key, instance );
        return this;
    }

    public <T> InstanceRegistry add( final Class<T> role, final String hint, final T instance )
    {
        if ( role == null )
        {
            throw new NullPointerException( "Role class is null." );
        }

        if ( instance == null )
        {
            throw new NullPointerException( "Instance is null." );
        }

        if ( !role.isAssignableFrom( instance.getClass() ) )
        {
            throw new IllegalArgumentException( "Instance class: " + instance.getClass().getName()
                            + " is not assignable to role: " + role.getClass() );
        }

        return add( new ComponentKey<T>( role, hint ), instance );
    }

    public <T> InstanceRegistry add( final Class<T> role, final T instance )
    {
        if ( role == null )
        {
            throw new NullPointerException( "Role class is null." );
        }

        if ( instance == null )
        {
            throw new NullPointerException( "Instance is null." );
        }

        if ( !role.isAssignableFrom( instance.getClass() ) )
        {
            throw new IllegalArgumentException( "Instance class: " + instance.getClass().getName()
                            + " is not assignable to role: " + role.getClass() );
        }

        return add( new ComponentKey<T>( role ), instance );
    }

    public <T> InstanceRegistry addVirtual( final ComponentKey<T> key, final VirtualInstance<T> instance )
    {
        instances.put( key, instance );
        return this;
    }

    public <T> InstanceRegistry addVirtual( final Class<T> role, final String hint, final VirtualInstance<T> instance )
    {
        if ( role == null )
        {
            throw new NullPointerException( "Role class is null." );
        }

        if ( instance == null )
        {
            throw new NullPointerException( "Instance is null." );
        }

        if ( !role.isAssignableFrom( instance.getClass() ) )
        {
            throw new IllegalArgumentException( "Instance class: " + instance.getClass().getName()
                            + " is not assignable to role: " + role.getClass() );
        }

        return addVirtual( new ComponentKey<T>( role, hint ), instance );
    }

    public <T> InstanceRegistry addVirtual( final Class<T> role, final VirtualInstance<T> instance )
    {
        if ( role == null )
        {
            throw new NullPointerException( "Role class is null." );
        }

        if ( instance == null )
        {
            throw new NullPointerException( "Instance is null." );
        }

        if ( !role.isAssignableFrom( instance.getClass() ) )
        {
            throw new IllegalArgumentException( "Instance class: " + instance.getClass().getName()
                            + " is not assignable to role: " + role.getClass() );
        }

        return addVirtual( new ComponentKey<T>( role ), instance );
    }

    public InstanceRegistry overrideMerge( final InstanceRegistry instanceRegistry )
    {
        if ( !instanceRegistry.instances.isEmpty() )
        {
            instances.putAll( instanceRegistry.instances );
        }

        return this;
    }

    public Map<ComponentKey<?>, Object> getInstances()
    {
        return instances;
    }

}
