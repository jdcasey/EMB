package org.commonjava.emb.conf;

import org.apache.log4j.Logger;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfigurationException;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;

import java.util.Map;
import java.util.Set;

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

public interface EMBLibrary
{

    Logger getLogger();

    ExtensionConfiguration getConfiguration();

    ComponentSelector getComponentSelector();

    Set<ComponentKey<?>> getExportedComponents();

    Set<ComponentKey<?>> getManagementComponents( Class<?> managementType );

    Map<Class<?>, Set<ComponentKey<?>>> getManagementComponents();

    String getLabel();

    String getId();

    String getLogHandle();

    String getName();

    String getVersion();

    void loadConfiguration( final EMBConfiguration embConfig )
        throws ExtensionConfigurationException;

}
