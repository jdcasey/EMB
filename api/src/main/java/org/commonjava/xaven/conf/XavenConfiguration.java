package org.commonjava.xaven.conf;

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

import org.commonjava.xaven.XavenExecutionRequest;
import org.commonjava.xaven.conf.ext.ExtensionConfiguration;
import org.commonjava.xaven.plexus.ComponentKey;
import org.commonjava.xaven.plexus.ComponentSelector;
import org.commonjava.xaven.plexus.InstanceRegistry;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class XavenConfiguration
{

    public static final String STANDARD_LOG_HANDLE_CORE = "core";

    public static final String STANDARD_LOG_HANDLE_LOADER = "xaven-loader";

    private static final File DEFAULT_CONFIGURATION_DIRECTORY = new File( System.getProperty( "user.home" ), ".m2" );

    private ComponentSelector componentSelector;

    private InstanceRegistry instanceRegistry;

    private Map<String, XavenLibrary> extensions;

    private File configurationDirectory = DEFAULT_CONFIGURATION_DIRECTORY;

    private XavenExecutionRequest executionRequest;

    private InputStream stdin = System.in;

    private PrintStream stdout = System.out;

    private PrintStream stderr = System.err;

    private boolean debug;

    private boolean interactive = true;

    public XavenConfiguration()
    {
    }

    public XavenConfiguration withXavenExecutionRequest( final XavenExecutionRequest request )
    {
        executionRequest = request;
        return this;
    }

    public XavenConfiguration withStandardIn( final InputStream stdin )
    {
        this.stdin = stdin;
        return this;
    }

    public InputStream getStandardIn()
    {
        return stdin;
    }

    public XavenConfiguration withStandardOut( final PrintStream stdout )
    {
        this.stdout = stdout;
        return this;
    }

    public PrintStream getStandardOut()
    {
        return stdout;
    }

    public XavenConfiguration withStandardErr( final PrintStream stderr )
    {
        this.stderr = stderr;
        return this;
    }

    public PrintStream getStandardErr()
    {
        return stderr;
    }

    public XavenExecutionRequest getXavenExecutionRequest()
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

    public XavenConfiguration withConfigurationDirectory( final File configurationDirectory )
    {
        this.configurationDirectory = configurationDirectory;
        return this;
    }

    public File getConfigurationDirectory()
    {
        return configurationDirectory;
    }

    public XavenConfiguration withLibraries( final Map<String, XavenLibrary> libraries )
    {
        getLibraries().putAll( libraries );
        for ( final XavenLibrary library : libraries.values() )
        {
            withLibrary( library );
        }
        return this;
    }

    public XavenLibrary getExtension( final String extId )
    {
        return extensions.get( extId );
    }

    public Map<String, XavenLibrary> getLibraries()
    {
        if ( extensions == null )
        {
            extensions = new HashMap<String, XavenLibrary>();
        }

        return extensions;
    }

    public ComponentSelector getComponentSelector()
    {
        if ( componentSelector == null )
        {
            componentSelector = new ComponentSelector();
        }

        return componentSelector;
    }

    public synchronized XavenConfiguration withComponentSelection( final ComponentKey key, final String newHint )
    {
        getComponentSelector().setSelection( key, newHint );
        return this;
    }

    public synchronized XavenConfiguration withComponentSelections( final Map<ComponentKey, String> selections )
    {
        if ( selections != null )
        {
            for ( final Map.Entry<ComponentKey, String> entry : selections.entrySet() )
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

    public synchronized XavenConfiguration withComponentSelections( final ComponentSelector newSelector )
    {
        if ( newSelector != null )
        {
            getComponentSelector().merge( newSelector );
        }

        return this;
    }

    public XavenConfiguration withComponentSelector( final ComponentSelector selector )
    {
        getComponentSelector().merge( selector );

        return this;
    }

    public XavenConfiguration withoutDebug()
    {
        debug = false;
        return this;
    }

    public XavenConfiguration withDebug()
    {
        debug = true;
        return this;
    }

    public XavenConfiguration interactive()
    {
        interactive = true;
        return this;
    }

    public XavenConfiguration nonInteractive()
    {
        interactive = false;
        return this;
    }

    public XavenConfiguration withLibrary( final XavenLibrary library )
    {
        getLibraries().put( library.getId(), library );
        withComponentSelector( library.getComponentSelector() );
        withComponentInstance( new ComponentKey( XavenLibrary.class, library.getId() ), library );

        final ExtensionConfiguration configuration = library.getConfiguration();
        if ( configuration != null )
        {
            withComponentInstance( new ComponentKey( configuration.getClass() ), configuration );
        }

        return this;
    }

    public synchronized XavenConfiguration withComponentInstance( final ComponentKey key, final Object instance )
    {
        getInstanceRegistry().add( key, instance );

        return this;
    }

    public synchronized XavenConfiguration withInstanceRegistry( final InstanceRegistry instanceRegistry )
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

        return instanceRegistry;
    }

}
