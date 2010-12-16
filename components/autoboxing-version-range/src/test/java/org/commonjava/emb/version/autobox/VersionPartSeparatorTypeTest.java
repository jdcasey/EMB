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

import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.DASH;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.DOT;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.NONE;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.compare;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.separatorTypeOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionPartSeparatorTypeTest
{

    @Test
    public void compare_DOT_to_DASH()
    {
        assertTrue( compare( DOT, DASH ) < 0 );
    }

    @Test
    public void compare_DASH_to_NONE()
    {
        assertTrue( compare( DASH, NONE ) == 0 );
    }

    @Test
    public void compare_DOT_to_NONE()
    {
        assertTrue( compare( DOT, NONE ) == 0 );
    }

    @Test
    public void separatorTypeOf_DOT()
    {
        assertEquals( DOT, separatorTypeOf( '.' ) );
    }

    @Test
    public void separatorTypeOf_DASH()
    {
        assertEquals( DASH, separatorTypeOf( '-' ) );
    }

    @Test
    public void separatorTypeOf_NONE()
    {
        assertEquals( NONE, separatorTypeOf( '_' ) );
        assertEquals( NONE, separatorTypeOf( '\n' ) );
        assertEquals( NONE, separatorTypeOf( '|' ) );
        assertEquals( NONE, separatorTypeOf( ':' ) );
        assertEquals( NONE, separatorTypeOf( 'A' ) );
        assertEquals( NONE, separatorTypeOf( '1' ) );
        assertEquals( NONE, separatorTypeOf( '\u0000' ) );
    }

}
