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

package org.commonjava.emb.components.aether;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;

public class AetherWrapperException
    extends Exception
{
    private static final long serialVersionUID = 1L;

    private final Object[] params;

    private final Collection<Throwable> causes;

    public AetherWrapperException( final String message, final Throwable cause, final Object... params )
    {
        super( message, cause );
        causes = null;
        this.params = params;
    }

    public AetherWrapperException( final String message, final Collection<Throwable> causes, final Object... params )
    {
        super( message );
        this.causes = causes;
        this.params = params;
    }

    public AetherWrapperException( final String message, final Object... params )
    {
        super( message );
        causes = null;
        this.params = params;
    }

    public AetherWrapperException( final String message, final Throwable cause )
    {
        super( message, cause );
        causes = null;
        params = null;
    }

    public AetherWrapperException( final String message, final Collection<Throwable> causes )
    {
        super( message );
        this.causes = causes;
        params = null;
    }

    public AetherWrapperException( final String message )
    {
        super( message );
        causes = null;
        params = null;
    }

    public Collection<Throwable> getCauses()
    {
        return causes;
    }

    public String getExtendedMessage()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( getMessage() );
        if ( getCause() != null || ( causes != null && !causes.isEmpty() ) )
        {
            sb.append( "\nCaused by:\n" );
        }

        if ( causes != null && !causes.isEmpty() )
        {
            for ( final Throwable cause : causes )
            {
                sb.append( "\n" ).append( cause.getClass().getName() ).append( ": " ).append( cause.getMessage() );
            }
        }

        if ( getCause() != null )
        {
            final Throwable cause = getCause();
            sb.append( "\n" ).append( cause.getClass().getName() ).append( ": " ).append( cause.getMessage() );
        }

        return sb.toString();
    }

    @Override
    public String getMessage()
    {
        final String format = super.getMessage();
        if ( params != null )
        {
            try
            {
                return String.format( format, params );
            }
            catch ( final Throwable t )
            {
                return format;
            }
        }
        else
        {
            return format;
        }
    }

    @Override
    public void printStackTrace()
    {
        printStackTrace( System.err );
    }

    @Override
    public void printStackTrace( final PrintStream s )
    {
        super.printStackTrace( s );
        if ( causes != null )
        {
            s.println( "\n\nCaused by " + causes.size() + " exceptions:\n" );
            int count = 0;
            for ( final Throwable cause : causes )
            {
                s.print( count + ".) " );
                cause.printStackTrace( s );
                count++;
            }
        }
    }

    @Override
    public void printStackTrace( final PrintWriter s )
    {
        super.printStackTrace( s );
        if ( causes != null )
        {
            s.println( "\n\nCaused by " + causes.size() + " exceptions:\n" );
            int count = 0;
            for ( final Throwable cause : causes )
            {
                s.print( count + ".) " );
                cause.printStackTrace( s );
                count++;
            }
        }
    }

}
