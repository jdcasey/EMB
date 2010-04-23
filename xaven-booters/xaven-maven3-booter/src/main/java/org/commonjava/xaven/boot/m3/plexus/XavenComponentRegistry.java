package org.commonjava.xaven.boot.m3.plexus;

import org.apache.log4j.Logger;
import org.codehaus.plexus.ComponentRegistry;
import org.codehaus.plexus.DefaultComponentRegistry;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.manager.ComponentManagerFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.lifecycle.LifecycleHandlerManager;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.conf.ext.ExtensionConfiguration;

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

public class XavenComponentRegistry
    implements ComponentRegistry
{

    private static final Logger logger = Logger.getLogger( XavenComponentRegistry.class );

    private final DefaultComponentRegistry delegate;

    private final Map<String, Object> configComponents = new HashMap<String, Object>();

    public XavenComponentRegistry( final MutablePlexusContainer container, final ComponentRepository repository,
                                   final LifecycleHandlerManager lifecycleHandlerManager,
                                   final XavenConfiguration config )
    {
        delegate = new DefaultComponentRegistry( container, repository, lifecycleHandlerManager );

        configComponents.put( XavenConfiguration.class.getName(), config );

        final Map<String, ? extends ExtensionConfiguration> ext = config.getExtensionConfigurations();
        if ( ext != null && !ext.isEmpty() )
        {
            for ( final Map.Entry<String, ? extends ExtensionConfiguration> entry : ext.entrySet() )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Adding configuration component with role: '" + entry.getValue().getClass().getName()
                        + "' and hint: 'default'." );
                }

                configComponents.put( entry.getValue().getClass().getName(), entry.getValue() );
            }
        }
    }

    public <T> void addComponent( final T component, final String role, final String roleHint )
    {
        delegate.addComponent( component, role, roleHint );
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
        if ( configComponents.containsKey( role ) )
        {
            return (T) configComponents.get( role );
        }

        return delegate.lookup( type, role, roleHint );
    }

    @SuppressWarnings( "unchecked" )
    public <T> T lookup( final ComponentDescriptor<T> componentDescriptor )
        throws ComponentLookupException
    {
        if ( configComponents.containsKey( componentDescriptor.getRole() ) )
        {
            return (T) configComponents.get( componentDescriptor.getRole() );
        }

        return delegate.lookup( componentDescriptor );
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

}
