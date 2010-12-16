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

package org.commonjava.emb.version.autobox;

import java.util.Arrays;
import java.util.List;

public enum VersionPartSeparatorType
{

    // NOTE: Order is used for sorting versions!
    DOT,
    DASH,
    NONE;

    private static final List<VersionPartSeparatorType> typeList = Arrays.asList( VersionPartSeparatorType.values() );

    public static int compare( final VersionPartSeparatorType one, final VersionPartSeparatorType two )
    {
        // NONE is a NOP in terms of separator ordering...it cannot sort higher or lower than another type.
        if ( ( one == two ) || ( one == NONE || two == NONE ) )
        {
            return 0;
        }

        return typeList.indexOf( one ) - typeList.indexOf( two );
    }

    public static VersionPartSeparatorType separatorTypeOf( final char c )
    {
        if ( c == '.' )
        {
            return DOT;
        }
        else if ( c == '-' )
        {
            return DASH;
        }

        return NONE;
    }

}
