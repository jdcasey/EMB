package org.commonjava.xaven.conf.mgmt;

import org.commonjava.xaven.conf.XavenConfiguration;

import java.util.List;
import java.util.Map;

public interface XavenManagementView
{

    <T> T lookup( Class<T> role, String hint )
        throws XavenManagementException;

    <T> T lookup( Class<T> role )
        throws XavenManagementException;

    <T> Map<String, T> lookupMap( Class<T> role, String... hints )
        throws XavenManagementException;

    <T> List<T> lookupList( Class<T> role, String... hints )
        throws XavenManagementException;

    XavenConfiguration getConfiguration();

}
