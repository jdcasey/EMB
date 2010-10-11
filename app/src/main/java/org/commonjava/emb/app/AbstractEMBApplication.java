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
import org.commonjava.emb.conf.VersionProvider;
import org.commonjava.emb.conf.ext.ExtensionConfigurationLoader;
import org.commonjava.emb.conf.loader.InstanceLibraryLoader;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;

public abstract class AbstractEMBApplication
    extends AbstractEMBLibrary
    implements EMBApplication
{

    private EMBEmbedderBuilder builder;

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final ComponentSelector componentSelector )
    {
        super( id, name, versionProvider, componentSelector );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final ExtensionConfigurationLoader configLoader,
                                      final ComponentSelector componentSelector )
    {
        super( id, name, versionProvider, configLoader, componentSelector );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final ExtensionConfigurationLoader configLoader )
    {
        super( id, name, versionProvider, configLoader );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle, final ComponentSelector componentSelector )
    {
        super( id, name, versionProvider, logHandle, componentSelector );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle, final ExtensionConfigurationLoader configLoader,
                                      final ComponentSelector componentSelector )
    {
        super( id, name, versionProvider, logHandle, configLoader, componentSelector );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle, final ExtensionConfigurationLoader configLoader )
    {
        super( id, name, versionProvider, logHandle, configLoader );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider,
                                      final String logHandle )
    {
        super( id, name, versionProvider, logHandle );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    protected AbstractEMBApplication( final String id, final String name, final VersionProvider versionProvider )
    {
        super( id, name, versionProvider );

        builder = new EMBEmbedderBuilder().withLibraryLoader( new InstanceLibraryLoader( this ) );
        initApp();
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    protected void initApp()
    {
        withComponentInstance( new ComponentKey( getClass() ), this );
    }

    protected final AbstractEMBApplication withEMBEmbedderBuilder( final EMBEmbedderBuilder builder )
    {
        this.builder = builder;
        return this;
    }

    protected final EMBEmbedderBuilder builder()
    {
        return builder;
    }

    public final void load()
        throws EMBException
    {
        builder.build();
        try
        {
            builder.container().lookup( getClass() );
        }
        catch ( final ComponentLookupException e )
        {
            throw new EMBException( "Failed to inject members of application. Reason: %s", e, e.getMessage() );
        }
    }
}
