#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.conf;

import org.commonjava.emb.conf.ext.ExtensionConfiguration;

public class AppIntegrationConfiguration
    implements ExtensionConfiguration
{

    private String serverUrl;

    private String user;

    private String pass;

    public AppIntegrationConfiguration withServerUrl( final String serverUrl )
    {
        this.serverUrl = serverUrl;
        return this;
    }

    public AppIntegrationConfiguration withCredentials( final String user, final String pass )
    {
        this.user = user;
        this.pass = pass;

        return this;
    }

    public String getServerUrl()
    {
        return serverUrl;
    }

    public void setServerUrl( final String serverUrl )
    {
        this.serverUrl = serverUrl;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser( final String user )
    {
        this.user = user;
    }

    public String getPass()
    {
        return pass;
    }

    public void setPass( final String pass )
    {
        this.pass = pass;
    }

}
