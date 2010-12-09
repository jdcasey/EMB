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

package org.commonjava.emb.internal.plexus;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Map;

public class MultiComponentLookupException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    private final Map<Object, Throwable> errors;

    public MultiComponentLookupException( final String message, final Map<Object, Throwable> errors )
    {
        super( "[" + errors.size() + " embedded errors]: " + message );
        this.errors = errors;
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

        int counter = 0;
        for ( final Map.Entry<Object, Throwable> entry : errors.entrySet() )
        {
            s.print( counter + ". " + entry.getKey() + ":" );
            entry.getValue().printStackTrace( s );
            s.println( "\n\n" );

            counter++;
        }
    }

    @Override
    public void printStackTrace( final PrintWriter s )
    {
        super.printStackTrace( s );

        int counter = 0;
        for ( final Map.Entry<Object, Throwable> entry : errors.entrySet() )
        {
            s.print( counter + ". " + entry.getKey() + ":" );
            entry.getValue().printStackTrace( s );
            s.println( "\n\n" );

            counter++;
        }
    }

    @Override
    public String getMessage()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append( super.getMessage() );

        int counter = 0;
        for ( final Map.Entry<Object, Throwable> entry : errors.entrySet() )
        {
            builder.append( counter )
                   .append( ". " )
                   .append( entry.getKey() )
                   .append( ":" )
                   .append( entry.getValue().getMessage() )
                   .append( "\n\n" );

            counter++;
        }

        return builder.toString();
    }

    @Override
    public String getLocalizedMessage()
    {
        return getMessage();
    }

}
