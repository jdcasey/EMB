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

package org.commonjava.emb.component.vscheme.conf;

import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.component.vscheme.VersionSchemeSelector;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.conf.mgmt.LoadOnFinish;
import org.commonjava.emb.conf.mgmt.LoadOnStart;
import org.commonjava.emb.plexus.ComponentKey;

/**
 * EMB library that injects a custom {@link MirrorSelector} implementation, which uses a custom
 * configuration loaded by {@link AppIntegrationConfigLoader}.
 */
@Service( EMBLibrary.class )
public class VersionSchemeInjectorLibrary
    extends AbstractEMBLibrary
{
    // Requires an empty constructor for ServiceLoader to work!
    public VersionSchemeInjectorLibrary()
    {
        super( "vscheme", "Version-Scheme-Injector", new MavenPomVersionProvider( "org.commonjava.emb.components",
                                                                                  "version-scheme-injector" ) );

        withManagementComponent( new ComponentKey( VersionSchemeSelector.class ), LoadOnStart.class, LoadOnFinish.class );
    }
}
