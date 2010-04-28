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

import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import org.apache.log4j.Logger;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRepository;
import org.codehaus.plexus.component.repository.DefaultComponentRepository;
import org.commonjava.xaven.conf.XavenConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SelectorComponentRepository
    implements ComponentRepository
{

    private static final Logger logger = Logger.getLogger( SelectorComponentRepository.class );

    private static final char LITERAL_HINT_DELIMITER = '_';

    private static final String BLANK_ROLE_HINT_PLACEHOLDER = "#";

    private final ComponentRepository delegateRepository;

    private final Properties componentSelectors;

    public SelectorComponentRepository( final XavenConfiguration xavenConfig )
    {
        componentSelectors = xavenConfig.getComponentSelections();
        delegateRepository = new DefaultComponentRepository();
    }

    public SelectorComponentRepository( final ComponentRepository delegateRepository,
                                        final XavenConfiguration xavenConfig )
    {
        componentSelectors = xavenConfig.getComponentSelections();
        this.delegateRepository = delegateRepository;
    }

    public void addComponentDescriptor( final ComponentDescriptor<?> componentDescriptor )
        throws CycleDetectedInComponentGraphException
    {
        delegateRepository.addComponentDescriptor( componentDescriptor );
    }

    public <T> ComponentDescriptor<T> getComponentDescriptor( final Class<T> type, final String role,
                                                              final String roleHint )
    {
        return delegateRepository.getComponentDescriptor( type, role, selectRoleHint( role, roleHint ) );
    }

    @Deprecated
    public ComponentDescriptor<?> getComponentDescriptor( final String role, final String roleHint,
                                                          final ClassRealm realm )
    {
        return delegateRepository.getComponentDescriptor( role, selectRoleHint( role, roleHint ), realm );
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

    private String selectRoleHint( final String role, final String roleHint )
    {
        final StringBuilder sb = new StringBuilder( role );

        if ( isNotBlank( roleHint ) )
        {
            if ( BLANK_ROLE_HINT_PLACEHOLDER.equals( roleHint ) )
            {
                return null;
            }
            else if ( roleHint.length() > 2 && roleHint.charAt( 0 ) == LITERAL_HINT_DELIMITER
                && roleHint.charAt( roleHint.length() - 1 ) == LITERAL_HINT_DELIMITER )
            {
                return roleHint.substring( 1, roleHint.length() - 1 );
            }

            sb.append( '#' ).append( roleHint );
        }

        String selectedHint = componentSelectors.getProperty( sb.toString() );
        if ( selectedHint == null && PlexusConstants.PLEXUS_DEFAULT_HINT.equals( roleHint ) )
        {
            selectedHint = componentSelectors.getProperty( role );
        }

        if ( selectedHint == null )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "No component override for role: '" + role + "', hint: '" + roleHint + "'." );
            }

            return roleHint;
        }
        else
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Replaced hint: '" + roleHint + "' with hint: '" + selectedHint + "' for role: '" + role
                    + "'." );
            }

            return selectedHint;
        }
    }
}
