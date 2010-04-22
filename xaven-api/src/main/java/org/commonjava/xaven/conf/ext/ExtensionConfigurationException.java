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

package org.commonjava.xaven.conf.ext;

import java.text.MessageFormat;

public class ExtensionConfigurationException
    extends Exception
{

    private static final long serialVersionUID = 1L;

    private final Object[] params;

    private String formattedMessage;

    public ExtensionConfigurationException( final String message, final Throwable cause )
    {
        super( message, cause );
        params = null;
    }

    public ExtensionConfigurationException( final String message )
    {
        super( message );
        params = null;
    }

    public ExtensionConfigurationException( final String message, final Throwable cause, final Object... params )
    {
        super( message, cause );
        this.params = params;
    }

    public ExtensionConfigurationException( final String message, final Object... params )
    {
        super( message );
        this.params = params;
    }

    @Override
    public synchronized String getMessage()
    {
        if ( formattedMessage == null )
        {
            final String format = super.getMessage();
            if ( params == null || params.length < 1 )
            {
                formattedMessage = format;
            }
            else
            {
                try
                {
                    formattedMessage = MessageFormat.format( format, params );
                }
                catch ( final Error e )
                {
                    formattedMessage = format;
                    throw e;
                }
                catch ( final RuntimeException e )
                {
                    formattedMessage = format;
                    throw e;
                }
                catch ( final Exception e )
                {
                    formattedMessage = format;
                }
            }
        }

        return formattedMessage;
    }

}
