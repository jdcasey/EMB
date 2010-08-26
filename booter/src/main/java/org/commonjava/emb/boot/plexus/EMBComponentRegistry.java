package org.commonjava.emb.boot.plexus;

import static org.codehaus.plexus.util.StringUtils.isBlank;

import org.codehaus.plexus.ComponentRegistry;
import org.codehaus.plexus.DefaultComponentRegistry;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.manager.ComponentManagerFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.lifecycle.LifecycleHandlerManager;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.InstanceRegistry;

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

public class EMBComponentRegistry
    implements ComponentRegistry
{

    private final DefaultComponentRegistry delegate;

    private final InstanceRegistry instanceRegistry;

    public EMBComponentRegistry( final MutablePlexusContainer container, final ComponentRepository repository,
                                   final LifecycleHandlerManager lifecycleHandlerManager,
                                   final EMBConfiguration config, final InstanceRegistry instanceRegistry )
    {
        delegate = new DefaultComponentRegistry( container, repository, lifecycleHandlerManager );

        this.instanceRegistry = instanceRegistry == null ? new InstanceRegistry( instanceRegistry ) : instanceRegistry;
        this.instanceRegistry.add( EMBConfiguration.class, config );
    }

    public <T> void addComponent( final T component, final String role, final String roleHint )
    {
        instanceRegistry.add( new ComponentKey( role, roleHint ), component );
    }

    public void addComponentDescriptor( final ComponentDescriptor<?> componentDescriptor )
        throws CycleDetectedInComponentGraphException
    {
        delegate.addComponentDescriptor( componentDescriptor );
    }

    public void dispose()
    {
        delegate.dispose();
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role,
                                                              final String roleHint )
    {
        return delegate.getComponentDescriptor( type, role, roleHint );
    }

    @SuppressWarnings( "deprecation" )
    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String roleHint,
                                                          final ClassRealm realm )
    {
        return delegate.getComponentDescriptor( role, roleHint, realm );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        return delegate.getComponentDescriptorList( type, role );
    }

    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( final Class<T> type, final String role )
    {
        return delegate.getComponentDescriptorMap( type, role );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T lookup( final Class<T> type, final String role, final String roleHint )
        throws ComponentLookupException
    {
        T result = (T) lookupEMBInstance( role, roleHint );

        if ( result == null )
        {
            final String hint = isBlank( roleHint ) ? PlexusConstants.PLEXUS_DEFAULT_HINT : roleHint;
            result = delegate.lookup( type, role, hint );
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    public <T> T lookup( final ComponentDescriptor<T> cd )
        throws ComponentLookupException
    {
        final T result = (T) lookupEMBInstance( cd.getRole(), cd.getRoleHint() );
        return result != null ? result : delegate.lookup( cd );
    }

    public <T> List<T> lookupList( final Class<T> type, final String role, final List<String> roleHints )
        throws ComponentLookupException
    {
        return delegate.lookupList( type, role, roleHints );
    }

    public <T> Map<String, T> lookupMap( final Class<T> type, final String role, final List<String> roleHints )
        throws ComponentLookupException
    {
        return delegate.lookupMap( type, role, roleHints );
    }

    public void registerComponentManagerFactory( final ComponentManagerFactory componentManagerFactory )
    {
        delegate.registerComponentManagerFactory( componentManagerFactory );
    }

    public void release( final Object component )
        throws ComponentLifecycleException
    {
        delegate.release( component );
    }

    public void removeComponentRealm( final ClassRealm classRealm )
        throws PlexusContainerException
    {
        delegate.removeComponentRealm( classRealm );
    }

    protected Object lookupEMBInstance( final String role, final String roleHint )
    {
        ComponentKey key = new ComponentKey( role, roleHint );
        if ( instanceRegistry.has( key ) )
        {
            return instanceRegistry.get( key );
        }
        else if ( PlexusConstants.PLEXUS_DEFAULT_HINT.equals( roleHint ) )
        {
            key = new ComponentKey( role );
            if ( instanceRegistry.has( key ) )
            {
                return instanceRegistry.get( key );
            }
        }

        return null;
    }
}
