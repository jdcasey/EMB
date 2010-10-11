package org.commonjava.emb.conf;

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

import org.commonjava.emb.EMBExecutionRequest;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;
import org.commonjava.emb.plexus.InstanceRegistry;
import org.commonjava.emb.plexus.ServiceAuthorizer;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EMBConfiguration
{

    public static final String STANDARD_LOG_HANDLE_CORE = "core";

    public static final String STANDARD_LOG_HANDLE_LOADER = "emb-loader";

    private static final File DEFAULT_CONFIGURATION_DIRECTORY = new File( System.getProperty( "user.home" ), ".m2" );

    private ComponentSelector componentSelector;

    private InstanceRegistry instanceRegistry;

    private Set<EMBLibrary> libraries;

    private File configurationDirectory = DEFAULT_CONFIGURATION_DIRECTORY;

    private EMBExecutionRequest executionRequest;

    private InputStream stdin = System.in;

    private PrintStream stdout = System.out;

    private PrintStream stderr = System.err;

    private boolean debug;

    private boolean interactive = true;

    public EMBConfiguration()
    {
    }

    public EMBConfiguration withEMBExecutionRequest( final EMBExecutionRequest request )
    {
        executionRequest = request;
        return this;
    }

    public EMBConfiguration withStandardIn( final InputStream stdin )
    {
        this.stdin = stdin;
        return this;
    }

    public InputStream getStandardIn()
    {
        return stdin;
    }

    public EMBConfiguration withStandardOut( final PrintStream stdout )
    {
        this.stdout = stdout;
        return this;
    }

    public PrintStream getStandardOut()
    {
        return stdout;
    }

    public EMBConfiguration withStandardErr( final PrintStream stderr )
    {
        this.stderr = stderr;
        return this;
    }

    public PrintStream getStandardErr()
    {
        return stderr;
    }

    public EMBExecutionRequest getEMBExecutionRequest()
    {
        return executionRequest;
    }

    public boolean isInteractive()
    {
        return interactive;
    }

    public boolean isDebugEnabled()
    {
        return debug;
    }

    public EMBConfiguration withConfigurationDirectory( final File configurationDirectory )
    {
        this.configurationDirectory = configurationDirectory;
        return this;
    }

    public File getConfigurationDirectory()
    {
        return configurationDirectory;
    }

    public EMBConfiguration withLibraries( final Collection<EMBLibrary> libraries )
    {
        for ( final EMBLibrary library : libraries )
        {
            withLibrary( library );
        }
        return this;
    }

    public EMBConfiguration withLibraries( final EMBLibrary... libraries )
    {
        for ( final EMBLibrary library : libraries )
        {
            withLibrary( library );
        }
        return this;
    }

    public EMBLibrary getLibrary( final String id )
    {
        for ( final EMBLibrary library : getLibraries() )
        {
            if ( library.getId().equalsIgnoreCase( id ) )
            {
                return library;
            }
        }

        return null;
    }

    public Set<EMBLibrary> getLibraries()
    {
        if ( libraries == null )
        {
            libraries = new HashSet<EMBLibrary>();
        }

        return libraries;
    }

    public ComponentSelector getComponentSelector()
    {
        if ( componentSelector == null )
        {
            componentSelector = new ComponentSelector();
        }

        return componentSelector;
    }

    public synchronized EMBConfiguration withComponentSelection( final ComponentKey<?> key, final String newHint )
    {
        getComponentSelector().setSelection( key, newHint );
        return this;
    }

    public synchronized EMBConfiguration withComponentSelections( final Map<ComponentKey<?>, String> selections )
    {
        if ( selections != null )
        {
            for ( final Map.Entry<ComponentKey<?>, String> entry : selections.entrySet() )
            {
                if ( entry == null || entry.getKey() == null || entry.getValue() == null )
                {
                    continue;
                }

                getComponentSelector().setSelection( entry.getKey(), entry.getValue() );
            }
        }

        return this;
    }

    public synchronized EMBConfiguration withComponentSelections( final ComponentSelector newSelector )
    {
        if ( newSelector != null )
        {
            getComponentSelector().merge( newSelector );
        }

        return this;
    }

    public EMBConfiguration withComponentSelector( final ComponentSelector selector )
    {
        getComponentSelector().merge( selector );

        return this;
    }

    public EMBConfiguration withoutDebug()
    {
        debug = false;
        return this;
    }

    public EMBConfiguration withDebug()
    {
        debug = true;
        return this;
    }

    public EMBConfiguration interactive()
    {
        interactive = true;
        return this;
    }

    public EMBConfiguration nonInteractive()
    {
        interactive = false;
        return this;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    public EMBConfiguration withLibrary( final EMBLibrary library )
    {
        getLibraries().add( library );
        withComponentSelector( library.getComponentSelector() );
        withInstanceRegistry( library.getInstanceRegistry() );
        withComponentInstance( new ComponentKey<EMBLibrary>( EMBLibrary.class, library.getId() ), library );

        final ExtensionConfiguration configuration = library.getConfiguration();
        if ( configuration != null )
        {
            withComponentInstance( new ComponentKey<ExtensionConfiguration>( ExtensionConfiguration.class,
                                                                             library.getId() ), configuration );

            withComponentInstance( new ComponentKey( configuration.getClass() ), configuration );
        }

        return this;
    }

    public synchronized <T> EMBConfiguration withComponentInstance( final ComponentKey<T> key, final T instance )
    {
        getInstanceRegistry().add( key, instance );

        return this;
    }

    public synchronized EMBConfiguration withInstanceRegistry( final InstanceRegistry instanceRegistry )
    {
        if ( instanceRegistry != null )
        {
            getInstanceRegistry().overrideMerge( instanceRegistry );
        }

        return this;
    }

    public synchronized InstanceRegistry getInstanceRegistry()
    {
        if ( instanceRegistry == null )
        {
            instanceRegistry = new InstanceRegistry();
        }

        final Set<ComponentKey<?>> keys = new HashSet<ComponentKey<?>>();
        for ( final EMBLibrary lib : getLibraries() )
        {
            final Set<ComponentKey<?>> exports = lib.getExportedComponents();
            if ( exports != null && !exports.isEmpty() )
            {
                keys.addAll( exports );
            }
        }

        instanceRegistry.add( new ComponentKey<ServiceAuthorizer>( ServiceAuthorizer.class ),
                              new ServiceAuthorizer( keys ) );
        instanceRegistry.add( EMBConfiguration.class, this );

        return instanceRegistry;
    }

}
