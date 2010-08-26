package org.commonjava.emb;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.conf.EMBLibrary;

import java.util.HashMap;
import java.util.Map;

@Component( role = EMBAdvisor.class )
public class EMBAdvisor
{

    private final Map<String, Object> advice = new HashMap<String, Object>();

    @Requirement( hint = "core" )
    private EMBLibrary library;

    public void clearAdvice()
    {
        if ( library.getLogger().isDebugEnabled() )
        {
            library.getLogger().debug( this + ": CLEAR ADVICE" );
        }
        advice.clear();
    }

    public EMBAdvisor advise( final String key, final Object value, final boolean override )
    {
        if ( override || !advice.containsKey( key ) )
        {
            if ( library.getLogger().isDebugEnabled() )
            {
                library.getLogger().debug( this + ": NEW ADVICE: " + key + " = " + value );
            }
            advice.put( key, value );
        }

        return this;
    }

    public Object getRawAdvice( final String key )
    {
        if ( library.getLogger().isDebugEnabled() )
        {
            library.getLogger().debug( this + ": GET ADVICE: " + key + " = " + advice.get( key ) );
        }
        return advice.get( key );
    }

    public <T> T getAdvice( final String key, final Class<T> adviceType )
        throws EMBException
    {
        try
        {
            return adviceType.cast( advice.get( key ) );
        }
        catch ( final ClassCastException e )
        {
            throw new EMBException( "Invalid type for advice: %s.\nExpected type: %s\nActual type: %s", e, key,
                                      adviceType.getName(), advice.get( key ).getClass().getName() );
        }
    }

}
