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

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.commonjava.emb.EMBException;
import org.commonjava.emb.boot.embed.EMBEmbedderBuilder;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.conf.VersionProvider;
import org.commonjava.emb.conf.ext.ExtensionConfigurationLoader;
import org.commonjava.emb.conf.loader.InstanceLibraryLoader;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEMBApplication
    extends AbstractEMBLibrary
    implements EMBApplication
{

    private EMBEmbedderBuilder builder;

    private final List<EMBLibrary> additionalLibraries = new ArrayList<EMBLibrary>();

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final ExtensionConfigurationLoader configLoader )
    {
        this( id, name, versionProvider, id, configLoader, null );
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle )
    {
        this( id, name, versionProvider, logHandle, null, null );
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider )
    {
        this( id, name, versionProvider, id, null, null );
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle, final ExtensionConfigurationLoader configLoader )
    {
        this( id, name, versionProvider, logHandle, configLoader, null );
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final ExtensionConfigurationLoader configLoader,
                                      final ComponentSelector componentSelector )
    {
        this( id, name, versionProvider, id, configLoader, componentSelector );
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle, final ComponentSelector componentSelector )
    {
        this( id, name, versionProvider, logHandle, null, componentSelector );
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final ComponentSelector componentSelector )
    {
        this( id, name, versionProvider, id, null, componentSelector );
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle, final ExtensionConfigurationLoader configLoader,
                                      final ComponentSelector componentSelector )
    {
        super( id, name, versionProvider, logHandle, configLoader, componentSelector );
        initializeApplication();
    }

    protected final AbstractEMBApplication withLibrary( final EMBLibrary library )
    {
        additionalLibraries.add( library );
        return this;
    }

    protected final AbstractEMBApplication withEMBEmbedderBuilder( final EMBEmbedderBuilder builder )
    {
        this.builder = builder;
        return this;
    }

    protected final EMBEmbedderBuilder builder()
    {
        if ( builder == null )
        {
            builder = new EMBEmbedderBuilder();

            builder.withLibraryLoader( new InstanceLibraryLoader( this ) );

            if ( !additionalLibraries.isEmpty() )
            {
                builder.withLibraryLoader( new InstanceLibraryLoader( additionalLibraries ) );
            }
        }

        return builder;
    }

    public final void load()
        throws EMBException
    {
        final EMBEmbedderBuilder builder = builder();

        beforeLoading();
        configureBuilder( builder );

        builder.build();
        try
        {
            builder.container().lookup( getClass() );
        }
        catch ( final ComponentLookupException e )
        {
            throw new EMBException( "Forced member-injection for application failed. Reason: %s", e, e.getMessage() );
        }

        for ( final ComponentKey<?> key : getInstanceRegistry().getInstances().keySet() )
        {
            if ( key.getRoleClass() == getClass() )
            {
                continue;
            }

            try
            {
                builder.container().lookup( key.getRole(), key.getHint() );
            }
            catch ( final ComponentLookupException e )
            {
                throw new EMBException( "Forced member-injection for registered instance: %s failed. Reason: %s", e,
                                        key, e.getMessage() );
            }
        }

        afterLoading();
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    protected void initializeApplication()
    {
        withComponentInstance( new ComponentKey( getClass() ), this );
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
    protected AbstractEMBApplication withExportedComponent( final ComponentKey<?> key )
    {
        super.withExportedComponent( key );
        return this;
    }

    @Override
    public AbstractEMBApplication withManagementComponent( final ComponentKey<?> key, final Class<?>... managementTypes )
    {
        super.withManagementComponent( key, managementTypes );
        return this;
    }

    @Override
    public <T> AbstractEMBApplication withComponentInstance( final ComponentKey<T> key, final T instance )
    {
        super.withComponentInstance( key, instance );
        return this;
    }
}
