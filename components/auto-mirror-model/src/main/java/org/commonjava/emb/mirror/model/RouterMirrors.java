/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.commonjava.emb.mirror.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RouterMirrors
    implements Iterable<RouterMirror>
{

    private RouterMirror selected;

    private final List<RouterMirror> mirrors = new ArrayList<RouterMirror>();

    private final transient Random random = new Random();

    private transient List<Integer> indexGrabBag;

    public synchronized RouterMirrors addMirror( final RouterMirror mirror )
    {
        if ( !mirrors.contains( mirror ) )
        {
            mirrors.add( mirror );
            indexGrabBag = null;
        }

        return this;
    }

    public RouterMirror getSelectedMirror()
    {
        return selected;
    }

    public RouterMirror getHighestPriorityMirror()
    {
        if ( mirrors.isEmpty() )
        {
            return null;
        }

        final List<RouterMirror> available = new ArrayList<RouterMirror>( mirrors );

        // sort by weight.
        Collections.sort( available );

        for ( final RouterMirror mirror : available )
        {
            // return the highest-priority ENABLED mirror.
            if ( mirror.isEnabled() )
            {
                selected = mirror;
                return mirror;
            }
        }

        return null;
    }

    public RouterMirror getWeightedRandomSuggestion()
    {
        if ( mirrors.isEmpty() )
        {
            return null;
        }

        synchronized ( this )
        {
            if ( indexGrabBag == null )
            {
                final List<Integer> gb = new ArrayList<Integer>();
                for ( int idx = 0; idx < mirrors.size(); idx++ )
                {
                    final RouterMirror mirror = mirrors.get( idx );
                    if ( !mirror.isEnabled() )
                    {
                        // only select from enabled mirrors.
                        continue;
                    }

                    if ( mirror.getWeight() < 1 )
                    {
                        // make sure this mirror has at least one chance of being picked.
                        gb.add( idx );
                    }
                    else
                    {
                        // if weight == 9, give this mirror 9 chances of being picked randomly.
                        for ( int i = 0; i < mirror.getWeight(); i++ )
                        {
                            gb.add( idx );
                        }
                    }
                }

                indexGrabBag = gb;
            }
        }

        // generate a random number that will correspond to an index stored in the index grab bag.
        int idx = Math.abs( random.nextInt() ) % indexGrabBag.size();

        // use that random number to select the index of the mirror in the mirrors list.
        idx = indexGrabBag.get( idx );

        // lookup the mirror instance associated with the index from the grab bag.
        selected = mirrors.get( idx );
        return selected;
    }

    public RouterMirrors clearSelected()
    {
        selected = null;
        return this;
    }

    public int size()
    {
        return mirrors.size();
    }

    public boolean isEmpty()
    {
        return mirrors.isEmpty();
    }

    public boolean contains( final RouterMirror o )
    {
        return mirrors.contains( o );
    }

    public Iterator<RouterMirror> iterator()
    {
        return mirrors.iterator();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( mirrors == null ) ? 0 : mirrors.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final RouterMirrors other = (RouterMirrors) obj;
        if ( mirrors == null )
        {
            if ( other.mirrors != null )
            {
                return false;
            }
        }
        else if ( !mirrors.equals( other.mirrors ) )
        {
            return false;
        }
        return true;
    }

}
