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

package org.commonjava.emb.mirror.conf;

import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.repository.MirrorSelector;
import org.commonjava.atservice.annotation.Service;
import org.commonjava.emb.conf.AbstractEMBLibrary;
import org.commonjava.emb.conf.EMBLibrary;
import org.commonjava.emb.conf.MavenPomVersionProvider;
import org.commonjava.emb.plexus.ComponentSelector;

@Service( EMBLibrary.class )
public class AutoMirrorLibrary
    extends AbstractEMBLibrary
{

    public static final String HINT = "automirror";

    public AutoMirrorLibrary()
    {
        super( HINT, "AutoMirror", new MavenPomVersionProvider( "org.commonjava.emb.components", "emb-auto-mirror" ),
               new AutoMirrorConfigLoader(),
               new ComponentSelector().setSelection( MirrorSelector.class, HINT )
                                      .setSelection( org.sonatype.aether.repository.MirrorSelector.class, HINT )
                                      .setSelection( LegacySupport.class, HINT ) );
    }

}
