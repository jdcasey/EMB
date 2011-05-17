/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.app;

import org.apache.log4j.Logger;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.commonjava.emb.EMBException;
import org.commonjava.emb.boot.embed.EMBEmbedderBuilder;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.conf.VersionProvider;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfigurationException;
import org.commonjava.emb.conf.loader.InstanceLibraryLoader;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;
import org.commonjava.emb.plexus.InstanceRegistry;
import org.commonjava.emb.plexus.VirtualInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractEMBApplication
    implements EMBApplication
{

    private final List<EMBLibrary> additionalLibraries = new ArrayList<EMBLibrary>();

    private final InstanceRegistry instanceRegistry = new InstanceRegistry();

    private transient boolean loaded = false;

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    protected AbstractEMBApplication()
    {
        withLibrary( this );
        withComponentInstance( new ComponentKey( getClass() ), this );
    }

    protected final AbstractEMBApplication withLibrary( final EMBLibrary library )
    {
        additionalLibraries.add( library );
        return this;
    }

    @Override
    public EMBApplication load()
        throws EMBException
    {
        return doLoad();
    }

    private synchronized EMBApplication doLoad()
        throws EMBException
    {
        if ( loaded )
        {
            return this;
        }

        final EMBEmbedderBuilder builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( additionalLibraries ) );

        beforeLoading();
        configureBuilder( builder );

        builder.build();
        for ( final ComponentKey<?> key : getInstanceRegistry().getInstances().keySet() )
        {
            try
            {
                builder.container().lookup( key.getRoleClass(), key.getHint() );
            }
            catch ( final ComponentLookupException e )
            {
                throw new EMBException( "Forced member-injection for registered instance: %s failed. Reason: %s", e,
                                        key, e.getMessage() );
            }
        }

        afterLoading();

        loaded = true;

        return this;
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    protected final void withComponentInstance( final Object instance )
    {
        getInstanceRegistry().add( new ComponentKey( instance.getClass() ), instance );
    }

    protected final <C> void withVirtualComponent( final Class<C> virtualClass )
    {
        getInstanceRegistry().addVirtual( new VirtualInstance<C>( virtualClass ) );
    }

    protected final <C, T extends C> void setVirtualInstance( final Class<C> virtualKey, final T instance )
    {
        getInstanceRegistry().setVirtualInstance( virtualKey, instance );
    }

    protected final <C> void withComponentInstance( final ComponentKey<C> componentKey, final C instance )
    {
        getInstanceRegistry().add( componentKey, instance );
    }

    protected final <C> void withVirtualComponent( final ComponentKey<C> virtualKey )
    {
        getInstanceRegistry().addVirtual( virtualKey, new VirtualInstance<C>( virtualKey.getRoleClass() ) );
    }

    protected final <C, T extends C> void setVirtualInstance( final ComponentKey<C> virtualKey, final T instance )
    {
        getInstanceRegistry().setVirtualInstance( virtualKey, instance );
    }

    protected void configureBuilder( final EMBEmbedderBuilder builder )
        throws EMBException
    {
    }

    protected void beforeLoading()
        throws EMBException
    {
    }

    protected void afterLoading()
        throws EMBException
    {
    }

    @Override
    public Logger getLogger()
    {
        return Logger.getLogger( getLogHandle() );
    }

    @Override
    public ExtensionConfiguration getConfiguration()
    {
        return null;
    }

    @Override
    public ComponentSelector getComponentSelector()
    {
        return null;
    }

    @Override
    public Set<ComponentKey<?>> getExportedComponents()
    {
        return null;
    }

    @Override
    public Set<ComponentKey<?>> getManagementComponents( final Class<?> managementType )
    {
        return null;
    }

    @Override
    public Map<Class<?>, Set<ComponentKey<?>>> getManagementComponents()
    {
        return null;
    }

    @Override
    public String getLabel()
    {
        return getName();
    }

    @Override
    public String getLogHandle()
    {
        return getId();
    }

    @Override
    public void loadConfiguration( final EMBConfiguration embConfig )
        throws ExtensionConfigurationException
    {
    }

    @Override
    public final InstanceRegistry getInstanceRegistry()
    {
        return instanceRegistry;
    }

    @Override
    public String getVersion()
    {
        final VersionProvider provider = getVersionProvider();
        if ( provider == null )
        {
            throw new IllegalStateException( "Your application booter: " + getClass().getName()
                            + " must implement either getVersion() or getVersionProvider()." );
        }

        return provider.getVersion();
    }

    protected VersionProvider getVersionProvider()
    {
        return null;
    }

}
