package org.commonjava.maven.plexus;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.factory.ComponentInstantiationException;
import org.codehaus.plexus.component.factory.java.JavaComponentFactory;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

/**
 * Based on JavaComponentFactory from plexus: Component Factory for components written in Java 
 * Language which have default no parameter constructor
 *
 * @author <a href="mailto:jdcasey@apache.org">John Casey</a>
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:mmaczka@interia.pl">Michal Maczka</a>
 * @version $Id$
 */
public class OverridableDelegatingJavaComponentFactory
    extends JavaComponentFactory
{

    @Override
    public Object newInstance( final ComponentDescriptor requestedComponentDescriptor, final ClassRealm classRealm,
                               final PlexusContainer container )
        throws ComponentInstantiationException
    {
        final ComponentDescriptor descriptorToUse =
            OverrideManager.overrideComponentDescriptor( requestedComponentDescriptor, classRealm, container );

        return super.newInstance( descriptorToUse, classRealm, container );
    }

}
