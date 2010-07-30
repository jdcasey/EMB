#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.conf;

import static org.codehaus.plexus.util.IOUtil.close;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import org.commonjava.xaven.conf.XavenConfiguration;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationException;
import org.commonjava.xaven.conf.ext.ExtensionConfigurationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class will load configuration properties from ~/.m2/app.properties. 
 */
public class AppIntegrationConfigLoader
    implements ExtensionConfigurationLoader
{

    private static final String CONFIG_FILENAME = "app.properties";

    private static final String KEY_URL = "server-url";

    private static final String KEY_USER = "user";

    private static final String KEY_PASSWORD = "password";

    public Class<AppIntegrationConfiguration> getExtensionConfigurationClass()
    {
        return AppIntegrationConfiguration.class;
    }

    public AppIntegrationConfiguration loadConfiguration( final XavenConfiguration xavenConfig )
        throws ExtensionConfigurationException
    {
        final File configFile = new File( xavenConfig.getConfigurationDirectory(), CONFIG_FILENAME );
        final AppIntegrationConfiguration config = new AppIntegrationConfiguration();

        if ( configFile.exists() )
        {
            FileInputStream stream = null;
            try
            {
                stream = new FileInputStream( configFile );
                final Properties p = new Properties();
                p.load( stream );

                config.withServerUrl( p.getProperty( KEY_URL ) );

                final String user = p.getProperty( KEY_USER );
                final String pass = p.getProperty( KEY_PASSWORD );

                if ( isNotBlank( user ) && isNotBlank( pass ) )
                {
                    config.withCredentials( user, pass );
                }
            }
            catch ( final IOException e )
            {
                throw new ExtensionConfigurationException( "Failed to read properties: '{0}' from: {1}${symbol_escape}nReason: {2}",
                                                           e, CONFIG_FILENAME, xavenConfig.getConfigurationDirectory(),
                                                           e.getMessage() );
            }
            finally
            {
                close( stream );
            }
        }

        return config;
    }

}
