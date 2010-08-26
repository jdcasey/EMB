package org.commonjava.emb.boot.plexus;

import org.codehaus.plexus.ComponentRegistry;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.manager.PerLookupComponentManagerFactory;
import org.codehaus.plexus.component.manager.SingletonComponentManagerFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.io.PlexusTools;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.container.initialization.ContainerInitializationContext;
import org.codehaus.plexus.container.initialization.ContainerInitializationException;
import org.codehaus.plexus.container.initialization.ContainerInitializationPhase;
import org.codehaus.plexus.lifecycle.LifecycleHandlerManager;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.plexus.InstanceRegistry;

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

public class InitEMBRegistryPhase
    implements ContainerInitializationPhase
{

    private final EMBConfiguration config;

    private final InstanceRegistry instanceRegistry;

    public InitEMBRegistryPhase( final EMBConfiguration config, final InstanceRegistry instanceRegistry )
    {
        this.config = config;
        this.instanceRegistry = instanceRegistry;
    }

    public void execute( final ContainerInitializationContext context )
        throws ContainerInitializationException
    {
        final ComponentRepository repository = getComponentRepository( context );

        final LifecycleHandlerManager lifecycleHandlerManager = getLifecycleHandlerManager( context );

        final ComponentRegistry componentRegistry =
            new EMBComponentRegistry( context.getContainer(), repository, lifecycleHandlerManager, config,
                                        instanceRegistry );

        componentRegistry.registerComponentManagerFactory( new PerLookupComponentManagerFactory() );

        componentRegistry.registerComponentManagerFactory( new SingletonComponentManagerFactory() );

        context.getContainer().setComponentRegistry( componentRegistry );
    }

    private ComponentRepository getComponentRepository( final ContainerInitializationContext context )
        throws ContainerInitializationException
    {
        final ComponentRepository repository = context.getContainerConfiguration().getComponentRepository();

        // Add the components defined in the container xml configuration
        try
        {
            final PlexusConfiguration configuration = context.getContainerXmlConfiguration();

            final PlexusConfiguration[] componentConfigurations =
                configuration.getChild( "components" ).getChildren( "component" );
            for ( final PlexusConfiguration componentConfiguration : componentConfigurations )
            {
                final ComponentDescriptor<?> componentDescriptor =
                    PlexusTools.buildComponentDescriptor( componentConfiguration, context.getContainer()
                                                                                         .getContainerRealm() );
                componentDescriptor.setRealm( context.getContainer().getContainerRealm() );
                repository.addComponentDescriptor( componentDescriptor );
            }
        }
        catch ( final PlexusConfigurationException e )
        {
            throw new ContainerInitializationException( "Error initializing component repository: "
                + "Cannot unmarshall component descriptor: ", e );
        }
        catch ( final CycleDetectedInComponentGraphException e )
        {
            throw new ContainerInitializationException( "A cycle has been detected in the components of the system: ",
                                                        e );
        }

        return repository;
    }

    private LifecycleHandlerManager getLifecycleHandlerManager( final ContainerInitializationContext context )
    {
        final LifecycleHandlerManager lifecycleHandlerManager =
            context.getContainerConfiguration().getLifecycleHandlerManager();
        lifecycleHandlerManager.initialize();
        return lifecycleHandlerManager;
    }
}
