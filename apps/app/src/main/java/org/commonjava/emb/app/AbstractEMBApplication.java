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

    private EMBEmbedderBuilder builder()
    {
        return new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( additionalLibraries ) );
    }

    @Override
    public final EMBApplication load()
        throws EMBException
    {
        return load( null );
    }

    @Override
    public final EMBApplication load( final EMBApplicationConfiguration configuration )
        throws EMBException
    {
        if ( loaded )
        {
            return this;
        }

        final EMBEmbedderBuilder builder = builder();

        beforeLoading();
        configureBuilder( builder );
        if ( configuration != null )
        {
            configuration.configureBuilder( builder );
        }

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

    protected final <C> void withComponentInstance( final ComponentKey<C> componentKey, final C instance )
    {
        getInstanceRegistry().add( componentKey, instance );
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
