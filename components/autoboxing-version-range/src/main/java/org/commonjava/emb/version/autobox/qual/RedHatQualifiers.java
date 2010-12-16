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

package org.commonjava.emb.version.autobox.qual;

public final class RedHatQualifiers
    implements Qualifiers
{

    private static final String[] ORDER = { "MILESTONE", "M", "ALPHA", "A", "BETA", "B", "RC", "CR", };

    private static final String REBUILD_INDICATOR = "REDHAT";

    public static final Qualifiers INSTANCE = new RedHatQualifiers();

    private RedHatQualifiers()
    {
    }

    @Override
    public String[] order()
    {
        return ORDER;
    }

    @Override
    public String rebuildIndicator()
    {
        return REBUILD_INDICATOR;
    }

}
