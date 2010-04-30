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

package org.commonjava.xaven.nexus.plugin.autoconf;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.plexus.appevents.Event;

import javax.inject.Inject;
import javax.inject.Named;

@Named( "autonx-startup-injector" )
public class InjectingEventInspector
    extends AbstractEventInspector
    implements EventInspector, Startable
{

    private static final Logger logger = LoggerFactory.getLogger( InjectingEventInspector.class );

    @Inject
    @Named( "multicast" )
    private NexusResponder multicast;

    public boolean accepts( final Event<?> evt )
    {
        return false;
    }

    public void inspect( final Event<?> evt )
    {
    }

    public void start()
    {
        logger.info( "Using responders:" );
        logger.info( "\tmulticast: " + multicast.getClass().getName() );
    }

    public void stop()
    {
    }

}
