package org.commonjava.xaven.boot.m3.plexus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;
import org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

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

public class SelectorComponentRepositoryTest
{

    @BeforeClass
    public static void setupLogging()
    {
        final Configurator log4jConfigurator = new Configurator()
        {
            @SuppressWarnings( "unchecked" )
            public void doConfigure( final URL notUsed, final LoggerRepository repo )
            {
                repo.getRootLogger().addAppender( new ConsoleAppender( new SimpleLayout() ) );

                final Enumeration<Logger> loggers = repo.getCurrentLoggers();
                while ( loggers.hasMoreElements() )
                {
                    loggers.nextElement().setLevel( Level.DEBUG );
                }
            }
        };

        log4jConfigurator.doConfigure( null, LogManager.getLoggerRepository() );
    }

    @Test
    public void componentSubstitutionWhenTargetHasRoleHint()
        throws CycleDetectedInComponentGraphException
    {
        final ComponentDescriptor<String> cd1 = new ComponentDescriptor<String>();
        cd1.setRole( "role" );
        cd1.setRoleHint( "hint" );
        cd1.setImplementationClass( String.class );

        final ComponentDescriptor<String> cd2 = new ComponentDescriptor<String>();
        cd2.setRole( "role" );
        cd2.setRoleHint( "other-hint" );
        cd2.setImplementationClass( String.class );

        final Properties selectors = new Properties();
        selectors.setProperty( "role#hint", "other-hint" );

        final SelectorComponentRepository repo = new SelectorComponentRepository( new ComponentSelector( selectors ) );

        repo.addComponentDescriptor( cd1 );
        repo.addComponentDescriptor( cd2 );

        final ComponentDescriptor<String> result = repo.getComponentDescriptor( String.class, "role", "hint" );
        assertNotNull( result );
        assertEquals( "other-hint", result.getRoleHint() );
    }

    @Test
    public void componentSubstitutionWhenTargetRoleHintIsMissing()
        throws CycleDetectedInComponentGraphException
    {
        final ComponentDescriptor<String> cd1 = new ComponentDescriptor<String>();
        cd1.setRole( "role" );
        cd1.setRoleHint( "hint" );
        cd1.setImplementationClass( String.class );

        final ComponentDescriptor<String> cd2 = new ComponentDescriptor<String>();
        cd2.setRole( "role" );
        cd2.setRoleHint( "other-hint" );
        cd2.setImplementationClass( String.class );

        final Properties selectors = new Properties();
        selectors.setProperty( "role", "other-hint" );

        final SelectorComponentRepository repo = new SelectorComponentRepository( new ComponentSelector( selectors ) );

        repo.addComponentDescriptor( cd1 );
        repo.addComponentDescriptor( cd2 );

        final ComponentDescriptor<String> result = repo.getComponentDescriptor( String.class, "role", null );
        assertNotNull( result );
        assertEquals( "other-hint", result.getRoleHint() );
    }

    @Test
    public void componentSubstitutionWhenTargetRoleHintIsBlankPlaceholder()
        throws CycleDetectedInComponentGraphException
    {
        final ComponentDescriptor<String> cd1 = new ComponentDescriptor<String>();
        cd1.setRole( "role" );
        cd1.setRoleHint( "hint" );
        cd1.setImplementationClass( String.class );

        final ComponentDescriptor<String> cd2 = new ComponentDescriptor<String>();
        cd2.setRole( "role" );
        cd2.setRoleHint( "other-hint" );
        cd2.setImplementationClass( String.class );

        final Properties selectors = new Properties();
        selectors.setProperty( "role", "other-hint" );

        final SelectorComponentRepository repo = new SelectorComponentRepository( new ComponentSelector( selectors ) );

        repo.addComponentDescriptor( cd1 );
        repo.addComponentDescriptor( cd2 );

        final ComponentDescriptor<String> result = repo.getComponentDescriptor( String.class, "role", "#" );
        assertNotNull( result );
        assertEquals( "hint", result.getRoleHint() );
    }

    @Test
    public void componentSubstitutionWhenTargetRoleHintIsLiteral()
        throws CycleDetectedInComponentGraphException
    {
        final ComponentDescriptor<String> cd1 = new ComponentDescriptor<String>();
        cd1.setRole( "role" );
        cd1.setRoleHint( "hint" );
        cd1.setImplementationClass( String.class );

        final ComponentDescriptor<String> cd2 = new ComponentDescriptor<String>();
        cd2.setRole( "role" );
        cd2.setRoleHint( "other-hint" );
        cd2.setImplementationClass( String.class );

        final Properties selectors = new Properties();
        selectors.setProperty( "role#hint", "other-hint" );

        final SelectorComponentRepository repo = new SelectorComponentRepository( new ComponentSelector( selectors ) );

        repo.addComponentDescriptor( cd1 );
        repo.addComponentDescriptor( cd2 );

        final ComponentDescriptor<String> result = repo.getComponentDescriptor( String.class, "role", "_hint_" );
        assertNotNull( result );
        assertEquals( "hint", result.getRoleHint() );
    }

}
