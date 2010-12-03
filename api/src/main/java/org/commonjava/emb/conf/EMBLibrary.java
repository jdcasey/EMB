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

package org.commonjava.emb.conf;

import org.apache.log4j.Logger;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.conf.ext.ExtensionConfigurationException;
import org.commonjava.emb.plexus.ComponentKey;
import org.commonjava.emb.plexus.ComponentSelector;
import org.commonjava.emb.plexus.InstanceRegistry;

import java.util.Map;
import java.util.Set;

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

    InstanceRegistry getInstanceRegistry();

}
