package org.commonjava.xaven.plexus;

import static org.codehaus.plexus.util.StringUtils.isBlank;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import org.apache.log4j.Logger;
import org.codehaus.plexus.PlexusConstants;

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

public class ComponentSelector
{

    private static final String BLANK_ROLE_HINT_PLACEHOLDER = "#";

    private static final char LITERAL_HINT_DELIMITER = '_';

    private static final Logger logger = Logger.getLogger( ComponentSelector.class );

    private final Properties selections;

    public ComponentSelector( final Properties selections )
    {
        this.selections = selections == null ? new Properties() : selections;
    }

    public ComponentSelector( final ComponentSelector selectorToCopy )
    {
        selections = new Properties();
        merge( selectorToCopy );
    }

    public ComponentSelector()
    {
        selections = new Properties();
    }

    public ComponentSelector merge( final ComponentSelector selectorToCopy )
    {
        if ( selectorToCopy != null && !selectorToCopy.isEmpty() )
        {
            selections.putAll( selectorToCopy.selections );
        }

        return this;
    }

    public boolean isEmpty()
    {
        return selections.isEmpty();
    }

    public String selectRoleHint( final String role, final String roleHint )
    {
        final StringBuilder sb = new StringBuilder( role );

        if ( isNotBlank( roleHint ) )
        {
            if ( BLANK_ROLE_HINT_PLACEHOLDER.equals( roleHint ) )
            {
                return null;
            }
            else if ( roleHint.length() > 2 && roleHint.charAt( 0 ) == LITERAL_HINT_DELIMITER
                && roleHint.charAt( roleHint.length() - 1 ) == LITERAL_HINT_DELIMITER )
            {
                return roleHint.substring( 1, roleHint.length() - 1 );
            }

            sb.append( '#' ).append( roleHint );
        }

        String selectedHint = selections.getProperty( sb.toString() );
        if ( selectedHint == null && PlexusConstants.PLEXUS_DEFAULT_HINT.equals( roleHint ) )
        {
            selectedHint = selections.getProperty( role );
        }

        if ( selectedHint == null )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "No component override for role: '" + role + "', hint: '" + roleHint + "'." );
            }

            return roleHint;
        }
        else
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Replaced hint: '" + roleHint + "' with hint: '" + selectedHint + "' for role: '" + role
                    + "'." );
            }

            return selectedHint;
        }
    }

    public ComponentSelector setSelection( final ComponentKey originalKey, final String newHint )
    {
        selections.setProperty( format( originalKey.getRole(), originalKey.getHint() ), newHint );
        return this;
    }

    public ComponentSelector setSelection( final Class<?> role, final String oldHint, final String newHint )
    {
        selections.setProperty( format( role.getName(), oldHint ), newHint );
        return this;
    }

    public ComponentSelector setSelection( final String role, final String oldHint, final String newHint )
    {
        selections.setProperty( format( role, oldHint ), newHint );
        return this;
    }

    public ComponentSelector setSelection( final Class<?> role, final String newHint )
    {
        selections.setProperty( format( role.getName(), null ), newHint );
        return this;
    }

    public ComponentSelector setSelection( final String role, final String newHint )
    {
        selections.setProperty( format( role, null ), newHint );
        return this;
    }

    private String format( final String role, final String hint )
    {
        return role + ( isBlank( hint ) || PlexusConstants.PLEXUS_DEFAULT_HINT.equals( hint ) ? "" : "#" + hint );
    }

}
