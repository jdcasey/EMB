package org.commonjava.emb.conf;

import org.apache.log4j.Logger;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfigurationException;
import org.commonjava.emb.conf.ext.ExtensionConfigurationLoader;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public abstract class AbstractEMBLibrary
    implements EMBLibrary
{

    private final String name;

    private final VersionProvider versionProvider;

    private final String logHandle;

    private final Logger logger;

    private final String id;

    private final ExtensionConfigurationLoader configLoader;

    private ExtensionConfiguration config;

    private final ComponentSelector selector;

    private final Set<ComponentKey> exportedComponents = new HashSet<ComponentKey>();

    private final Map<Class<?>, Set<ComponentKey>> managementComponents = new HashMap<Class<?>, Set<ComponentKey>>();

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider,
                                    final ExtensionConfigurationLoader configLoader )
    {
        this( id, name, versionProvider, id, configLoader, null );
    }

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider,
                                    final String logHandle )
    {
        this( id, name, versionProvider, logHandle, null, null );
    }

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider )
    {
        this( id, name, versionProvider, id, null, null );
    }

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider,
                                    final String logHandle, final ExtensionConfigurationLoader configLoader )
    {
        this( id, name, versionProvider, logHandle, configLoader, null );
    }

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider,
                                    final ExtensionConfigurationLoader configLoader,
                                    final ComponentSelector componentSelector )
    {
        this( id, name, versionProvider, id, configLoader, componentSelector );
    }

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider,
                                    final String logHandle, final ComponentSelector componentSelector )
    {
        this( id, name, versionProvider, logHandle, null, componentSelector );
    }

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider,
                                    final ComponentSelector componentSelector )
    {
        this( id, name, versionProvider, id, null, componentSelector );
    }

    protected AbstractEMBLibrary( final String id, final String name, final VersionProvider versionProvider,
                                    final String logHandle, final ExtensionConfigurationLoader configLoader,
                                    final ComponentSelector componentSelector )
    {
        this.id = id;
        this.name = name;
        this.versionProvider = versionProvider;
        this.logHandle = logHandle;
        this.configLoader = configLoader;
        selector = componentSelector;
        logger = Logger.getLogger( logHandle );
    }

    public ComponentSelector getComponentSelector()
    {
        return selector;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void loadConfiguration( final EMBConfiguration embConfig )
        throws ExtensionConfigurationException
    {
        if ( configLoader != null )
        {
            config = configLoader.loadConfiguration( embConfig );
        }
    }

    public ExtensionConfiguration getConfiguration()
    {
        return config;
    }

    public String getLabel()
    {
        return name + ": " + versionProvider.getVersion();
    }

    public String getId()
    {
        return id;
    }

    public String getLogHandle()
    {
        return logHandle;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return versionProvider.getVersion();
    }

    protected AbstractEMBLibrary withExportedComponent( final ComponentKey key )
    {
        exportedComponents.add( key );
        return this;
    }

    @Override
    public Set<ComponentKey> getExportedComponents()
    {
        return exportedComponents;
    }

    public Map<Class<?>, Set<ComponentKey>> getManagementComponents()
    {
        return managementComponents;
    }

    public AbstractEMBLibrary withManagementComponent( final ComponentKey key, final Class<?>... managementTypes )
    {
        if ( managementTypes != null )
        {
            for ( final Class<?> managementType : managementTypes )
            {
                Set<ComponentKey> keys = managementComponents.get( managementType );
                if ( keys == null )
                {
                    keys = new HashSet<ComponentKey>();
                    managementComponents.put( managementType, keys );
                }
                keys.add( key );
            }
        }

        return this;
    }

    public Set<ComponentKey> getManagementComponents( final Class<?> managementType )
    {
        return managementComponents.get( managementType );
    }

}