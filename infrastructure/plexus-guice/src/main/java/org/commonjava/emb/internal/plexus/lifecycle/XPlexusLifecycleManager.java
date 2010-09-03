/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.commonjava.emb.internal.plexus.lifecycle;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link PlexusBeanManager} that manages Plexus components requiring lifecycle management.
 */
public final class XPlexusLifecycleManager
    implements PlexusBeanManager
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<Object> activeBeans = new ArrayList<Object>();

    private final PlexusContainer container;

    private final Context context;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public XPlexusLifecycleManager( final PlexusContainer container, final Context context )
    {
        this.container = container;
        this.context = context;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public boolean manage( final Class<?> clazz )
    {
        return LogEnabled.class.isAssignableFrom( clazz ) || Contextualizable.class.isAssignableFrom( clazz )
            || Initializable.class.isAssignableFrom( clazz ) || Startable.class.isAssignableFrom( clazz )
            || Disposable.class.isAssignableFrom( clazz );
    }

    @SuppressWarnings( "rawtypes" )
    public PropertyBinding manage( final BeanProperty property )
    {
        final Class clazz = property.getType().getRawType();
        if ( "org.slf4j.Logger".equals( clazz.getName() ) )
        {
            return new PropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
                public <B> void injectProperty( final B bean )
                {
                    property.set( bean, org.slf4j.LoggerFactory.getLogger( bean.getClass() ) );
                }
            };
        }
        if ( Logger.class.equals( clazz ) )
        {
            return new PropertyBinding()
            {
                @SuppressWarnings( "unchecked" )
                public <B> void injectProperty( final B bean )
                {
                    property.set( bean, getLogger( bean.getClass().getName() ) );
                }
            };
        }
        return null;
    }

    public boolean manage( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
            /*
             * Run through the startup phase of the standard plexus "personality"
             */
            if ( bean instanceof LogEnabled )
            {
                ( (LogEnabled) bean ).enableLogging( getLogger( name ) );
            }
            if ( bean instanceof Contextualizable )
            {
                ( (Contextualizable) bean ).contextualize( context );
            }
            if ( bean instanceof Initializable )
            {
                ( (Initializable) bean ).initialize();
            }
            synchronized ( this )
            {
                if ( bean instanceof Startable )
                {
                    ( (Startable) bean ).start();
                    activeBeans.add( bean );
                }
                else if ( bean instanceof Disposable )
                {
                    activeBeans.add( bean );
                }
            }
        }
        catch ( final Throwable e )
        {
            getLogger( name ).error( "Problem starting: " + bean, e );
        }

        return true;
    }

    public synchronized boolean unmanage( final Object bean )
    {
        return activeBeans.remove( bean ) ? dispose( bean ) : false;
    }

    public synchronized boolean unmanage()
    {
        boolean result = false;
        while ( !activeBeans.isEmpty() )
        {
            // dispose in reverse order of startup sequence
            result |= dispose( activeBeans.remove( activeBeans.size() - 1 ) );
        }
        return result;
    }

    public PlexusBeanManager manageChild()
    {
        return this;
    }

    // ----------------------------------------------------------------------
    // Shared implementation methods
    // ----------------------------------------------------------------------

    Logger getLogger( final String name )
    {
        return container.getLoggerManager().getLoggerForComponent( name, null );
    }

    void releaseLogger( final String name )
    {
        container.getLoggerManager().returnComponentLogger( name, null );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    private boolean dispose( final Object bean )
    {
        final String name = bean.getClass().getName();
        try
        {
            /*
             * Run through the shutdown phase of the standard plexus "personality"
             */
            if ( bean instanceof Startable )
            {
                ( (Startable) bean ).stop();
            }
            if ( bean instanceof Disposable )
            {
                ( (Disposable) bean ).dispose();
            }
        }
        catch ( final Throwable e )
        {
            getLogger( name ).error( "Problem stopping: " + bean, e );
        }
        finally
        {
            releaseLogger( name );
        }
        return true;
    }
}
