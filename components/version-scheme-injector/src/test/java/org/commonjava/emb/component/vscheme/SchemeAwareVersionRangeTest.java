/*
 *  Copyright (C) 2010 John Casey.
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonjava.emb.component.vscheme;

import static org.commonjava.emb.component.vscheme.SchemeAwareVersionRange.isRange;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SchemeAwareVersionRangeTest
{

    @Test
    public void inclusiveSimpleRange()
    {
        assertTrue( isRange( "[1.0,2.0]" ) );
    }

    @Test
    public void exclusiveSimpleRange()
    {
        assertTrue( isRange( "(1.0,2.0)" ) );
    }

    @Test
    public void inLeftExRightComplexRange()
    {
        assertTrue( isRange( "[1.0,2.0)" ) );
    }

    @Test
    public void exLeftInRightComplexRange()
    {
        assertTrue( isRange( "(1.0,2.0]" ) );
    }

    @Test
    public void multiRange()
    {
        assertTrue( isRange( "[1.0,1.1)(1.1,2.0]" ) );
    }

    @Test
    public void leftSpecifiedRange()
    {
        assertTrue( isRange( "(1.1,]" ) );
    }

    @Test
    public void rightSpecifiedRange()
    {
        assertTrue( isRange( "[,1.1)" ) );
    }

}
