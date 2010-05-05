package org.commonjava.xaven.boot.m3.plexus;

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

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.discovery.ComponentDiscoverer;
import org.codehaus.plexus.component.discovery.ComponentDiscovererManager;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.discovery.DefaultComponentDiscoverer;
import org.codehaus.plexus.component.discovery.DefaultComponentDiscovererManager;
import org.codehaus.plexus.component.discovery.PlexusXmlComponentDiscoverer;
import org.codehaus.plexus.component.factory.ComponentFactoryManager;
import org.codehaus.plexus.component.factory.DefaultComponentFactoryManager;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.configuration.source.ConfigurationSource;
import org.codehaus.plexus.container.initialization.ContainerInitializationPhase;
import org.codehaus.plexus.container.initialization.InitializeComponentDiscovererManagerPhase;
import org.codehaus.plexus.container.initialization.InitializeComponentFactoryManagerPhase;
import org.codehaus.plexus.container.initialization.InitializeContainerConfigurationSourcePhase;
import org.codehaus.plexus.container.initialization.InitializeLoggerManagerPhase;
import org.codehaus.plexus.container.initialization.InitializeSystemPropertiesPhase;
import org.codehaus.plexus.container.initialization.InitializeUserConfigurationSourcePhase;
import org.codehaus.plexus.lifecycle.BasicLifecycleHandler;
import org.codehaus.plexus.lifecycle.DefaultLifecycleHandlerManager;
import org.codehaus.plexus.lifecycle.LifecycleHandler;
import org.codehaus.plexus.lifecycle.LifecycleHandlerManager;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ConfigurablePhase;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.ContextualizePhase;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.DisposePhase;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializePhase;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.LogDisablePhase;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.LogEnablePhase;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartPhase;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StopPhase;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.plexus.ComponentSelector;
import org.commonjava.xaven.plexus.InstanceRegistry;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XavenContainerConfiguration
    implements ContainerConfiguration
{
    private String name;

    private Map<Object, Object> context;

    private ClassWorld classWorld;

    private ClassRealm realm;

    private String containerConfiguration;

    private URL containerConfigurationURL;

    private ConfigurationSource configurationSource;

    private ComponentDiscovererManager componentDiscovererManager;

    private LifecycleHandlerManager lifecycleHandlerManager;

    private ComponentFactoryManager componentFactoryManager;

    private SelectorComponentRepository componentRepository;

    @SuppressWarnings( "unchecked" )
    private final List<Class> componentDiscoverers = new ArrayList<Class>();

    @SuppressWarnings( "unchecked" )
    private final List<Class> componentDiscoveryListeners = new ArrayList<Class>();

    private final XavenConfiguration xavenConfig;

    private final InstanceRegistry instanceRegistry;

    private final ComponentSelector selector;

    public XavenContainerConfiguration( final XavenConfiguration xavenConfig, final ComponentSelector selector,
                                        final InstanceRegistry instanceRegistry )
    {
        this.xavenConfig = xavenConfig;
        this.selector = selector;
        this.instanceRegistry = instanceRegistry;
    }

    public ContainerConfiguration setName( final String name )
    {
        this.name = name;

        return this;
    }

    public ContainerConfiguration setContext( final Map<Object, Object> context )
    {
        this.context = context;

        return this;
    }

    public ContainerConfiguration setClassWorld( final ClassWorld classWorld )
    {
        this.classWorld = classWorld;

        return this;
    }

    public ContainerConfiguration setRealm( final ClassRealm realm )
    {
        this.realm = realm;

        return this;
    }

    public ContainerConfiguration setContainerConfiguration( final String containerConfiguration )
    {
        this.containerConfiguration = containerConfiguration;

        return this;
    }

    public String getContainerConfiguration()
    {
        return containerConfiguration;
    }

    public ContainerConfiguration setContainerConfigurationURL( final URL containerConfiguration )
    {
        containerConfigurationURL = containerConfiguration;

        return this;
    }

    public URL getContainerConfigurationURL()
    {
        return containerConfigurationURL;
    }

    public String getName()
    {
        return name;
    }

    public Map<Object, Object> getContext()
    {
        return context;
    }

    public ClassWorld getClassWorld()
    {
        return classWorld;
    }

    public ClassRealm getRealm()
    {
        return realm;
    }

    // Programmatic Container Initialization and Setup

    public ContainerInitializationPhase[] getInitializationPhases()
    {
        return new ContainerInitializationPhase[] { new InitXavenRegistryPhase( xavenConfig, instanceRegistry ),
            new InitializeComponentFactoryManagerPhase(), new InitializeContainerConfigurationSourcePhase(),
            new InitializeLoggerManagerPhase(), new InitializeSystemPropertiesPhase(),
            new InitializeComponentDiscovererManagerPhase(), new InitializeUserConfigurationSourcePhase()

        };
    }

    // Component discoverer

    public ContainerConfiguration addComponentDiscoveryListener(
                                                                 final ComponentDiscoveryListener componentDiscoveryListener )
    {
        getComponentDiscovererManager().registerComponentDiscoveryListener( componentDiscoveryListener );

        return this;
    }

    public ContainerConfiguration addComponentDiscoverer( final ComponentDiscoverer componentDiscoverer )
    {
        ( (DefaultComponentDiscovererManager) getComponentDiscovererManager() ).addComponentDiscoverer( componentDiscoverer );

        return this;
    }

    public ContainerConfiguration addComponentDiscoverer( final Class<?> clazz )
    {
        componentDiscoverers.add( clazz );
        return this;
    }

    public ContainerConfiguration addComponentDiscoveryListener( final Class<?> clazz )
    {
        componentDiscoveryListeners.add( clazz );
        return this;
    }

    @SuppressWarnings( "unchecked" )
    public List<Class> getComponentDiscoverers()
    {
        return componentDiscoverers;
    }

    @SuppressWarnings( "unchecked" )
    public List<Class> getComponentDiscoveryListeners()
    {
        return componentDiscoveryListeners;
    }

    public ContainerConfiguration setComponentDiscovererManager(
                                                                 final ComponentDiscovererManager componentDiscovererManager )
    {
        this.componentDiscovererManager = componentDiscovererManager;

        return this;
    }

    public ComponentDiscovererManager getComponentDiscovererManager()
    {
        if ( componentDiscovererManager == null )
        {
            final DefaultComponentDiscovererManager cdm = new DefaultComponentDiscovererManager();

            cdm.addComponentDiscoverer( new DefaultComponentDiscoverer() );
            cdm.addComponentDiscoverer( new PlexusXmlComponentDiscoverer() );

            componentDiscovererManager = cdm;
        }

        return componentDiscovererManager;
    }

    public ComponentFactoryManager getComponentFactoryManager()
    {
        if ( componentFactoryManager == null )
        {
            componentFactoryManager = new DefaultComponentFactoryManager();
        }

        return componentFactoryManager;
    }

    public ContainerConfiguration setComponentFactoryManager( final ComponentFactoryManager componentFactoryManager )
    {
        this.componentFactoryManager = componentFactoryManager;

        return this;
    }

    public ContainerConfiguration setComponentRepository( final ComponentRepository componentRepository )
    {
        this.componentRepository = new SelectorComponentRepository( componentRepository, selector );

        return this;
    }

    public ComponentRepository getComponentRepository()
    {
        if ( componentRepository == null )
        {
            componentRepository = new SelectorComponentRepository( selector );
        }

        return componentRepository;
    }

    // Lifecycle handler manager

    public ContainerConfiguration addLifecycleHandler( final LifecycleHandler lifecycleHandler )
    {
        getLifecycleHandlerManager().addLifecycleHandler( lifecycleHandler );

        return this;
    }

    public ContainerConfiguration setLifecycleHandlerManager( final LifecycleHandlerManager lifecycleHandlerManager )
    {
        this.lifecycleHandlerManager = lifecycleHandlerManager;

        return this;
    }

    public LifecycleHandlerManager getLifecycleHandlerManager()
    {
        if ( lifecycleHandlerManager == null )
        {
            lifecycleHandlerManager = new DefaultLifecycleHandlerManager();

            // Plexus
            final LifecycleHandler plexus = new BasicLifecycleHandler( "plexus" );
            // Begin
            plexus.addBeginSegment( new LogEnablePhase() );
            plexus.addBeginSegment( new ContextualizePhase() );
            //            plexus.addBeginSegment( new AutoConfigurePhase() );
            plexus.addBeginSegment( new InitializePhase() );
            plexus.addBeginSegment( new StartPhase() );
            // End
            plexus.addEndSegment( new StopPhase() );
            plexus.addEndSegment( new DisposePhase() );
            plexus.addEndSegment( new LogDisablePhase() );
            lifecycleHandlerManager.addLifecycleHandler( plexus );

            // Basic
            final LifecycleHandler basic = new BasicLifecycleHandler( "basic" );
            // Begin
            basic.addBeginSegment( new LogEnablePhase() );
            basic.addBeginSegment( new ContextualizePhase() );
            //            basic.addBeginSegment( new AutoConfigurePhase() );
            basic.addBeginSegment( new InitializePhase() );
            basic.addBeginSegment( new StartPhase() );
            // End
            basic.addEndSegment( new StopPhase() );
            basic.addEndSegment( new DisposePhase() );
            basic.addEndSegment( new LogDisablePhase() );
            lifecycleHandlerManager.addLifecycleHandler( basic );

            // Plexus configurable
            final LifecycleHandler plexusConfigurable = new BasicLifecycleHandler( "plexus-configurable" );
            // Begin
            plexusConfigurable.addBeginSegment( new LogEnablePhase() );
            plexusConfigurable.addBeginSegment( new ContextualizePhase() );
            plexusConfigurable.addBeginSegment( new ConfigurablePhase() );
            plexusConfigurable.addBeginSegment( new InitializePhase() );
            plexusConfigurable.addBeginSegment( new StartPhase() );
            // End
            plexusConfigurable.addEndSegment( new StopPhase() );
            plexusConfigurable.addEndSegment( new DisposePhase() );
            plexusConfigurable.addEndSegment( new LogDisablePhase() );
            lifecycleHandlerManager.addLifecycleHandler( plexusConfigurable );

            // Passive
            final LifecycleHandler passive = new BasicLifecycleHandler( "passive" );
            lifecycleHandlerManager.addLifecycleHandler( passive );

            // Bootstrap
            final LifecycleHandler bootstrap = new BasicLifecycleHandler( "bootstrap" );
            bootstrap.addBeginSegment( new ContextualizePhase() );
            lifecycleHandlerManager.addLifecycleHandler( bootstrap );
        }

        return lifecycleHandlerManager;
    }

    // Configuration Sources

    public ContainerConfiguration setConfigurationSource( final ConfigurationSource configurationSource )
    {
        this.configurationSource = configurationSource;

        return this;
    }

    public ConfigurationSource getConfigurationSource()
    {
        return configurationSource;
    }
}
