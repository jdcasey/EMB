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

import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.NONE;
import static org.commonjava.emb.version.autobox.VersionPartType.QUALIFIER;

import java.util.ArrayList;
import java.util.List;

public class VersionPart
    implements Comparable<VersionPart>
{

    private final VersionPartType type;

    private final VersionPartSeparatorType separatorType;

    private final Character separator;

    private final String rawPart;

    private final List<String> qualifiers;

    public VersionPart( final VersionPartType type, final String part, final List<String> qualifiers )
    {
        this.type = type;
        rawPart = part;
        this.qualifiers = normalize( qualifiers );
        separator = null;
        separatorType = NONE;
    }

    public VersionPart( final VersionPartType type, final String part, final VersionPartSeparatorType separatorType,
                        final Character separator, final List<String> qualifiers )
    {
        this.type = type;
        rawPart = part;
        this.separatorType = separatorType;
        this.separator = separator;
        this.qualifiers = normalize( qualifiers );
    }

    private List<String> normalize( final List<String> q )
    {
        final List<String> qualifiers = new ArrayList<String>();
        for ( final String val : q )
        {
            qualifiers.add( val.toUpperCase() );
        }

        return qualifiers;
    }

    @Override
    public int compareTo( final VersionPart o )
    {
        final int sepComp = VersionPartSeparatorType.compare( separatorType, o.separatorType );
        if ( sepComp == 0 )
        {
            final int idxComp = VersionPartType.compare( type, o.type );

            if ( idxComp == 0 )
            {
                final Integer partComp = type.compareParts( rawPart, o.rawPart );
                if ( partComp == null )
                {
                    if ( type == QUALIFIER )
                    {
                        final int myQ = qualifiers.indexOf( rawPart.toUpperCase() );
                        final int otherQ = qualifiers.indexOf( o.rawPart.toUpperCase() );

                        return myQ - otherQ;
                    }
                }
                else
                {
                    return partComp;
                }
            }

            return idxComp;
        }

        return sepComp;
    }

    @Override
    public String toString()
    {
        return separator == null ? rawPart : separator + rawPart;
    }

    public VersionPartType getType()
    {
        return type;
    }

    public VersionPartSeparatorType getSeparatorType()
    {
        return separatorType;
    }

    public Character getSeparator()
    {
        return separator;
    }

    public String getRawPart()
    {
        return rawPart;
    }

    public List<String> getQualifiers()
    {
        return qualifiers;
    }

}
