package org.commonjava.xaven.conf;

import org.apache.log4j.Logger;
import org.commonjava.xaven.conf.ext.ExtensionConfiguration;

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

public final class XavenLibrary
{

    private final String name;

    private final String version;

    private final ExtensionConfiguration config;

    private final String logHandle;

    private final Logger logger;

    private final String id;

    XavenLibrary( final String id, final String name, final String version, final String logHandle,
                    final ExtensionConfiguration config )
    {
        this.id = id;
        this.name = name;
        this.version = version;
        this.logHandle = logHandle;
        this.config = config;
        logger = Logger.getLogger( logHandle );
    }

    public Logger getLogger()
    {
        return logger;
    }

    public ExtensionConfiguration getConfiguration()
    {
        return config;
    }

    public String getLabel()
    {
        return name + ": " + version;
    }

    public String getId()
    {
        return id;
    }

    public String getLogHandle()
    {
        return logHandle;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

}
