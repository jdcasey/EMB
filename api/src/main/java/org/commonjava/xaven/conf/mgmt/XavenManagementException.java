package org.commonjava.xaven.conf.mgmt;

import org.commonjava.xaven.XavenException;

public class XavenManagementException
    extends XavenException
{

    private static final long serialVersionUID = 1L;

    public XavenManagementException( final String message, final Object... params )
    {
        super( message, params );
    }

    public XavenManagementException( final String message, final Throwable cause, final Object... params )
    {
        super( message, cause, params );
    }

    public XavenManagementException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public XavenManagementException( final String message )
    {
        super( message );
    }

}
