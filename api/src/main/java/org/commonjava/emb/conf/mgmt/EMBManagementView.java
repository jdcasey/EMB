package org.commonjava.emb.conf.mgmt;

import org.commonjava.emb.conf.EMBConfiguration;

import java.util.List;
import java.util.Map;

public interface EMBManagementView
{

    <T> T lookup( Class<T> role, String hint )
        throws EMBManagementException;

    <T> T lookup( Class<T> role )
        throws EMBManagementException;

    <T> Map<String, T> lookupMap( Class<T> role )
        throws EMBManagementException;

    <T> List<T> lookupList( Class<T> role )
        throws EMBManagementException;

    EMBConfiguration getConfiguration();

}
