package org.commonjava.xaven;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.xaven.conf.XavenLibrary;

import java.util.HashMap;
import java.util.Map;

@Component( role = XavenAdvisor.class )
public class XavenAdvisor
{

    private final Map<String, Object> advice = new HashMap<String, Object>();

    @Requirement( hint = "core" )
    private XavenLibrary library;

    public void clearAdvice()
    {
        advice.clear();
    }

    public XavenAdvisor advise( final String key, final Object value, final boolean override )
    {
        if ( override || !advice.containsKey( key ) )
        {
            library.getLogger().debug( "NEW ADVICE: " + key + " = " + value );
            advice.put( key, value );
        }

        return this;
    }

    public Object getRawAdvice( final String key )
    {
        return advice.get( key );
    }

    public <T> T getAdvice( final String key, final Class<T> adviceType )
        throws XavenException
    {
        try
        {
            return adviceType.cast( advice.get( key ) );
        }
        catch ( final ClassCastException e )
        {
            throw new XavenException( "Invalid type for advice: %s.\nExpected type: %s\nActual type: %s", e, key,
                                      adviceType.getName(), advice.get( key ).getClass().getName() );
        }
    }

}
