package org.commonjava.maven.plexus;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.exception.ComponentRepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

// NOTE: This code attempts to be excessively paranoid, since if it fails we have a MAJOR problem!
public class OverrideManager
{
    private static final String OVERRIDES_PROPERTIES_PATH = "META-INF/maven/component-overrides.properties";

    private static final Properties overrides = new Properties();

    private static final Logger logger = Logger.getLogger( OverridableJavaComponentFactory.class.getName() );

    private static final Map<String, ComponentDescriptor> resolvedDescriptors =
        new HashMap<String, ComponentDescriptor>();

    private static final Map<String, String> overrideKeys = new HashMap<String, String>();

    static
    {
        final ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        if ( cloader != null )
        {
            Enumeration<URL> resources = null;
            try
            {
                resources = cloader.getResources( OVERRIDES_PROPERTIES_PATH );
            }
            catch ( final IOException e )
            {
                if ( logger.isLoggable( Level.FINE ) )
                {
                    final LogRecord record =
                        new LogRecord( Level.FINE, "Failed to lookup classpath resources: " + OVERRIDES_PROPERTIES_PATH
                            + "\nReason: " + e.getMessage() );

                    record.setThrown( e );

                    logger.log( record );
                }
            }

            if ( resources != null )
            {
                while ( resources.hasMoreElements() )
                {
                    final URL resource = resources.nextElement();

                    if ( resource != null )
                    {
                        InputStream stream = null;
                        try
                        {
                            final Properties p = new Properties();
                            stream = resource.openStream();
                            p.load( stream );

                            overrides.putAll( p );
                        }
                        catch ( final IOException e )
                        {
                            if ( logger.isLoggable( Level.FINE ) )
                            {
                                final LogRecord record =
                                    new LogRecord( Level.FINE, "Failed to read component overrides from: "
                                        + OVERRIDES_PROPERTIES_PATH + "\nReason: " + e.getMessage() );

                                record.setThrown( e );

                                logger.log( record );
                            }
                        }
                        finally
                        {
                            if ( stream != null )
                            {
                                try
                                {
                                    stream.close();
                                }
                                catch ( final IOException e )
                                {
                                }
                            }
                        }
                    }
                }
            }
        }

        if ( !overrides.isEmpty() && logger.isLoggable( Level.FINE ) )
        {
            logger.fine( "Loaded: " + overrides.size() + " component overrides." );
        }
    }

    public static String findOverride( final String componentKey )
    {
        String result = overrideKeys.get( componentKey );
        if ( result == null )
        {
            result = componentKey;

            final String override = overrides.getProperty( componentKey );
            if ( override != null )
            {
                final int idx = override.indexOf( '#' );
                if ( idx > -1 )
                {
                    result = override.substring( 0, idx ) + override.substring( idx + 1 );
                }
            }

            overrideKeys.put( componentKey, result );
        }

        return result;
    }

    public static ComponentDescriptor overrideComponentDescriptor(
                                                                   final ComponentDescriptor requestedComponentDescriptor,
                                                                   final ClassRealm classRealm,
                                                                   final PlexusContainer container )
    {
        final String key =
            requestedComponentDescriptor.getRole()
                + ( isEmpty( requestedComponentDescriptor.getRoleHint() ) ? ""
                                : requestedComponentDescriptor.getRoleHint() );

        ComponentDescriptor descriptorToUse = resolvedDescriptors.get( key );
        if ( descriptorToUse != null )
        {
            return descriptorToUse;
        }

        String role = null;
        String hint = null;

        String replacementKey = null;
        if ( overrides != null && ( replacementKey = overrides.getProperty( key ) ) != null )
        {
            final String[] keyParts = replacementKey.split( "#" );
            if ( keyParts == null || keyParts.length < 2 || isEmpty( keyParts[0] ) || isEmpty( keyParts[1] ) )
            {
                if ( logger.isLoggable( Level.FINER ) )
                {
                    final LogRecord r =
                        new LogRecord( Level.FINER, "Invalid override: " + replacementKey
                            + ". Must be of format: 'role#hint'." );

                    logger.log( r );
                }
            }
            else
            {
                role = keyParts[0];
                hint = keyParts[1];
            }
        }

        final String replacementComponentKey = role + hint;
        if ( isNotEmpty( role ) && isNotEmpty( hint ) && container.hasComponent( replacementComponentKey ) )
        {
            if ( logger.isLoggable( Level.FINER ) )
            {
                final LogRecord r =
                    new LogRecord( Level.FINER, "Overriding component.\nRequested hint: "
                        + requestedComponentDescriptor.getRoleHint() + "\nRequested role: "
                        + requestedComponentDescriptor.getRole() + "\nOverride hint: " + hint + "\nOverride role: "
                        + role );

                logger.log( r );
            }

            descriptorToUse = container.getComponentDescriptor( replacementComponentKey );

            final String overriddenOriginalHint = "OVERRIDDEN-" + requestedComponentDescriptor.getRoleHint();
            final String overriddenOriginalRole = "OVERRIDDEN-" + requestedComponentDescriptor.getRole();

            if ( !requestedComponentDescriptor.equals( descriptorToUse )
                && !container.hasComponent( overriddenOriginalRole, overriddenOriginalHint ) )
            {
                final ComponentDescriptor cdo = new ComponentDescriptor();
                cloneComponentDescriptorData( requestedComponentDescriptor, cdo, "OVERRIDDEN-" );

                try
                {
                    container.addComponentDescriptor( cdo );
                }
                catch ( final ComponentRepositoryException e )
                {
                    if ( logger.isLoggable( Level.FINE ) )
                    {
                        final LogRecord r =
                            new LogRecord( Level.FINE,
                                           "Failed to create new component descriptor to provide access to overridden compoent: "
                                               + requestedComponentDescriptor.getRole() + ":"
                                               + requestedComponentDescriptor.getRoleHint()
                                               + ". New hint should have been: " + overriddenOriginalHint
                                               + "\nReason: " + e.getMessage() );

                        r.setThrown( e );
                        logger.log( r );
                    }
                }
            }

            cloneComponentDescriptorData( descriptorToUse, requestedComponentDescriptor, null );

            resolvedDescriptors.put( key, descriptorToUse );
            return descriptorToUse;
        }

        resolvedDescriptors.put( key, requestedComponentDescriptor );
        return requestedComponentDescriptor;
    }

    @SuppressWarnings( "unchecked" )
    private static void cloneComponentDescriptorData( final ComponentDescriptor from, final ComponentDescriptor to,
                                                      final String prefix )
    {
        if ( prefix != null )
        {
            to.setRole( prefix + from.getRole() );

            if ( isNotEmpty( from.getRoleHint() ) )
            {
                to.setRoleHint( prefix + from.getRoleHint() );
            }

            if ( isNotEmpty( from.getAlias() ) )
            {
                to.setAlias( prefix + from.getAlias() );
            }
        }

        to.setImplementation( from.getImplementation() );
        to.setComponentComposer( from.getComponentComposer() );
        to.setComponentConfigurator( from.getComponentConfigurator() );
        to.setComponentFactory( from.getComponentFactory() );
        to.setComponentProfile( from.getComponentProfile() );
        to.setComponentSetDescriptor( from.getComponentSetDescriptor() );
        to.setComponentType( from.getComponentType() );
        to.setConfiguration( from.getConfiguration() );
        to.setDescription( from.getDescription() );
        to.setInstantiationStrategy( from.getInstantiationStrategy() );
        to.setIsolatedRealm( from.isIsolatedRealm() );
        to.setLifecycleHandler( from.getLifecycleHandler() );
        to.setVersion( from.getVersion() );

        final List<ComponentRequirement> replRequirements = from.getRequirements();
        to.getRequirements().clear();
        if ( replRequirements != null && !replRequirements.isEmpty() )
        {
            for ( final ComponentRequirement req : replRequirements )
            {
                to.addRequirement( req );
            }
        }
    }

    private static boolean isEmpty( final String str )
    {
        return str == null || str.trim().length() < 1;
    }

    private static boolean isNotEmpty( final String str )
    {
        return !isEmpty( str );
    }

}
