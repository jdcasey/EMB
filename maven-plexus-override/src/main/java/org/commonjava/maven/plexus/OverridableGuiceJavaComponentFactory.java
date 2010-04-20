package org.commonjava.maven.plexus;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.discovery.tools.Service;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.factory.ComponentInstantiationException;
import org.codehaus.plexus.component.factory.java.JavaComponentFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.commonjava.maven.plexus.conf.XavenGuiceModule;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Based on JavaComponentFactory from plexus: Component Factory for components written in Java 
 * Language which have default no parameter constructor
 * 
 * This implementation uses Guice to lookup the base instance before passing it to Plexus for further
 * composition, if needed.
 *
 * @author <a href="mailto:jdcasey@apache.org">John Casey</a>
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:mmaczka@interia.pl">Michal Maczka</a>
 * @version $Id$
 */
@SuppressWarnings( "unchecked" )
public class OverridableGuiceJavaComponentFactory
    extends JavaComponentFactory
{

    private static final Injector injector;

    static
    {
        final Enumeration<XavenGuiceModule> providers = Service.providers( XavenGuiceModule.class );
        final ArrayList<XavenGuiceModule> providerList = Collections.list( providers );
        injector = Guice.createInjector( providerList );
    }

    @Override
    public Object newInstance( final ComponentDescriptor requestedComponentDescriptor, final ClassRealm classRealm,
                               final PlexusContainer container )
        throws ComponentInstantiationException
    {
        final ComponentDescriptor descriptorToUse =
            OverrideManager.overrideComponentDescriptor( requestedComponentDescriptor, classRealm, container );

        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        final Class<?> impl;
        try
        {
            impl = cloader.loadClass( descriptorToUse.getImplementation() );
        }
        catch ( final ClassNotFoundException e )
        {
            throw new ComponentInstantiationException( "Cannot load implementation class: "
                + descriptorToUse.getImplementation() + "\nReason: " + e.getMessage(), e );
        }

        final String key =
            descriptorToUse.getRole()
                + ( descriptorToUse.getRoleHint() == null ? "" : "#" + descriptorToUse.getRoleHint() );

        final Injector childInjector = injector.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Object.class ).annotatedWith( Names.named( key ) ).to( impl );
            }
        } );

        try
        {
            return childInjector.getInstance( Key.get( Object.class, Names.named( key ) ) );
        }
        catch ( final ConfigurationException e )
        {
            throw new ComponentInstantiationException( "Failed to instantiate: " + impl.getName() + "\nRole: "
                + descriptorToUse.getRole() + "\nHint: " + descriptorToUse.getRoleHint() + "\nReason: "
                + e.getMessage(), e );
        }
        catch ( final ProvisionException e )
        {
            throw new ComponentInstantiationException( "Failed to instantiate: " + impl.getName() + "\nRole: "
                + descriptorToUse.getRole() + "\nHint: " + descriptorToUse.getRoleHint() + "\nReason: "
                + e.getMessage(), e );
        }
    }

}
