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

package org.commonjava.emb.version.autobox.lib;

import org.commonjava.emb.conf.ext.ExtensionConfiguration;

public class AutoboxingConfig
    implements ExtensionConfiguration
{

    private final boolean autoBox;

    private final String[] qualifierOrder;

    private final String rebuildQualifier;

    public AutoboxingConfig( final boolean autoBox, final String rebuildQualifier, final String... qualifierOrder )
    {
        this.autoBox = autoBox;
        this.rebuildQualifier = rebuildQualifier;
        this.qualifierOrder = qualifierOrder;
    }

    public boolean isAutoBox()
    {
        return autoBox;
    }

    public String[] getQualifierOrder()
    {
        // make the ordering immutable.
        final String[] result = new String[qualifierOrder.length];
        System.arraycopy( qualifierOrder, 0, result, 0, qualifierOrder.length );

        return result;
    }

    public String getRebuildQualifier()
    {
        return rebuildQualifier;
    }

}
