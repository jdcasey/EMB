package org.commonjava.xaven.conf.mgmt;

import org.commonjava.xaven.conf.XavenConfiguration;

public interface XavenManagementView
{

    <T> T lookup( Class<T> role, String hint )
        throws XavenManagementException;

    <T> T lookup( Class<T> role )
        throws XavenManagementException;

    XavenConfiguration getConfiguration();

}
