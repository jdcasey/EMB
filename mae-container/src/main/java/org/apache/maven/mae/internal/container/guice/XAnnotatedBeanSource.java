/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package org.apache.maven.mae.internal.container.guice;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.scanners.PlexusAnnotatedMetadata;

import java.util.Map;

final class XAnnotatedBeanSource
    implements PlexusBeanSource
{
    private final PlexusBeanMetadata metadata;

    XAnnotatedBeanSource( final Map<?, ?> variables )
    {
        metadata = new PlexusAnnotatedMetadata( variables );
    }

    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        return implementation.isAnnotationPresent( Component.class ) ? metadata : null;
    }
}
