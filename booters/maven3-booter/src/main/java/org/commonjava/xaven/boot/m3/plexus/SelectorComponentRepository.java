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

import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.DefaultComponentRepository;
import org.commonjava.xaven.plexus.ComponentSelector;

import java.util.List;
import java.util.Map;

public class SelectorComponentRepository
    implements ComponentRepository
{

    private final ComponentRepository delegateRepository;

    private final ComponentSelector selector;

    public SelectorComponentRepository( final ComponentSelector selector )
    {
        this( null, selector );
    }

    public SelectorComponentRepository( final ComponentRepository delegateRepository, final ComponentSelector selector )
    {
        this.selector = selector == null ? new ComponentSelector() : selector;
        this.delegateRepository = delegateRepository == null ? new DefaultComponentRepository() : delegateRepository;
    }

    public void addComponentDescriptor( final ComponentDescriptor<?> componentDescriptor )
        throws CycleDetectedInComponentGraphException
    {
        delegateRepository.addComponentDescriptor( componentDescriptor );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role,
                                                              final String roleHint )
    {
        return delegateRepository.getComponentDescriptor( type, role, selector.selectRoleHint( role, roleHint ) );
    }

    @Deprecated
    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String roleHint,
                                                          final ClassRealm realm )
    {
        return delegateRepository.getComponentDescriptor( role, selector.selectRoleHint( role, roleHint ), realm );
    }

    public <T> List<ComponentDescriptor<T>> getComponentDescriptorList( final Class<T> type, final String role )
    {
        return delegateRepository.getComponentDescriptorList( type, role );
    }

    public <T> Map<String, ComponentDescriptor<T>> getComponentDescriptorMap( final Class<T> type, final String role )
    {
        return delegateRepository.getComponentDescriptorMap( type, role );
    }

    public void removeComponentRealm( final ClassRealm classRealm )
    {
        delegateRepository.removeComponentRealm( classRealm );
    }

}
