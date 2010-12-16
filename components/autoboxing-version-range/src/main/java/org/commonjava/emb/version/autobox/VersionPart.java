/*
 * Copyright (c) 2010 Red Hat, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see 
 * <http://www.gnu.org/licenses>.
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
