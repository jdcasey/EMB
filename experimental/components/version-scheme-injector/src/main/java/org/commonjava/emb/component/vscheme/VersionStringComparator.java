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

package org.commonjava.emb.component.vscheme;


import java.util.Comparator;

public class VersionStringComparator
    implements Comparator<String>
{

    private final VersionScheme scheme;

    public VersionStringComparator( final VersionScheme scheme )
    {
        this.scheme = scheme;
    }

    @Override
    public int compare( final String one, final String two )
    {
        final VersionComparison first = scheme.getComparableVersion( one );
        final VersionComparison second = scheme.getComparableVersion( two );
        return first.compareTo( second );
    }

}