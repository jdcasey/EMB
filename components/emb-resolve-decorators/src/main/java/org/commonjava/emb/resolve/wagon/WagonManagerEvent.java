/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonjava.emb.resolve.wagon;

import org.commonjava.emb.resolve.event.Event;

import java.util.HashMap;
import java.util.Map;

public class WagonManagerEvent
    implements Event
{

    private final WagonEventType eventType;

    private final Map<String, Object> parameters;

    public WagonManagerEvent( final WagonEventType eventType, final Map<String, Object> parameters )
    {
        this.eventType = eventType;
        this.parameters = new HashMap<String, Object>( parameters );
    }

    public WagonManagerEvent withAdditionalParameter( final String key, final Object value )
    {
        parameters.put( key, value );
        return this;
    }

    public WagonEventType getEventType()
    {
        return eventType;
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

}
