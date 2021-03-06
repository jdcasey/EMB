/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.conf.EMBLibrary;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.HashMap;
import java.util.Map;

@Component( role = EMBAdvisor.class )
public class EMBAdvisor
{

    private final Map<String, Object> advice = new HashMap<String, Object>();

    @Requirement( hint = "core" )
    private final EMBLibrary library;

    @Inject
    public EMBAdvisor( @Named( "core" ) final EMBLibrary library )
    {
        this.library = library;
    }

    public EMBAdvisor advise( final String key, final Object value, final boolean override )
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
