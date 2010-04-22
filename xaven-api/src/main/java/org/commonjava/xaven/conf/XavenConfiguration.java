package org.commonjava.xaven.conf;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.commonjava.xaven.conf.ext.ExtensionConfiguration;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public class XavenConfiguration
{

    private Properties componentSelections;

    private Map<String, ? extends ExtensionConfiguration> configs;

    private File configurationDirectory;

    public XavenConfiguration withConfigurationDirectory( final File configurationDirectory )
    {
        this.configurationDirectory = configurationDirectory;
        return this;
    }

    public File getConfigurationDirectory()
    {
        return configurationDirectory;
    }

    public XavenConfiguration withExtensionConfigurations( final Map<String, ? extends ExtensionConfiguration> configs )
    {
        this.configs = configs;
        return this;
    }

    @SuppressWarnings( "unchecked" )
    public <T extends ExtensionConfiguration> T getExtensionConfiguration( final Class<T> configClass )
    {
        return (T) configs.get( configClass.getName() );
    }

    public Map<String, ? extends ExtensionConfiguration> getExtensionConfigurations()
    {
        return configs;
    }

    public Properties getComponentSelections()
    {
        return componentSelections == null ? new Properties() : componentSelections;
    }

    public XavenConfiguration withComponentSelections( final Properties componentSelections )
    {
        this.componentSelections = componentSelections;
        return this;
    }

}
