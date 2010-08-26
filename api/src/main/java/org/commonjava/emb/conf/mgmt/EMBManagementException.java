package org.commonjava.emb.conf.mgmt;

import org.commonjava.emb.EMBException;

public class EMBManagementException
    extends EMBException
{

    private static final long serialVersionUID = 1L;

    public EMBManagementException( final String message, final Object... params )
    {
        super( message, params );
    }

    public EMBManagementException( final String message, final Throwable cause, final Object... params )
    {
        super( message, cause, params );
    }

    public EMBManagementException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public EMBManagementException( final String message )
    {
        super( message );
    }

}
