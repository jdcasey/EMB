package org.commonjava.xaven.boot.m3.plexus;

import org.codehaus.plexus.PlexusConstants;
import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.plexus.ComponentKey;
import org.commonjava.xaven.plexus.InstanceRegistry;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;

import com.google.inject.Binder;

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

public class XavenBeanModule
    implements PlexusBeanModule
{

    private final InstanceRegistry instanceRegistry;

    public XavenBeanModule( final XavenConfiguration config, final InstanceRegistry instanceRegistry )
    {
        this.instanceRegistry = instanceRegistry == null ? new InstanceRegistry( instanceRegistry ) : instanceRegistry;
        this.instanceRegistry.add( XavenConfiguration.class, config );
    }

    public <T> void addComponent( final T component, final String role, final String roleHint )
    {
        instanceRegistry.add( new ComponentKey( role, roleHint ), component );
    }

    protected Object lookupXavenInstance( final String role, final String roleHint )
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

    @Override
    public PlexusBeanSource configure( final Binder binder )
    {
        // TODO Implement PlexusBeanModule.configure
        throw new UnsupportedOperationException( "Not Implemented." );
    }
}
