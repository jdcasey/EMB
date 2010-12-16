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

import static org.commonjava.emb.version.autobox.AutoboxableVersionScheme.MAX_REBUILD_NUMBER;
import static org.commonjava.emb.version.autobox.VersionPartSeparatorType.DASH;
import static org.commonjava.emb.version.autobox.VersionPartType.INT;
import static org.commonjava.emb.version.autobox.VersionPartType.REBUILD;

import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AutoboxableVersion
    implements Version
{

    private final VersionPart[] parts;

    private final boolean isSnapshot;

    private final List<String> qualifiers;

    private final String rebuildIndicator;

    public AutoboxableVersion( final boolean isSnapshot, final String rebuildIndicator, final List<String> qualifiers,
                               final VersionPart... parts )
    {
        this.isSnapshot = isSnapshot;
        this.rebuildIndicator = rebuildIndicator;
        this.qualifiers = qualifiers;
        this.parts = parts;
    }

    @Override
    public int compareTo( final Version o )
    {
        if ( this == o )
        {
            return 0;
        }

        final AutoboxableVersion ov;
        if ( o instanceof AutoboxableVersion )
        {
            ov = (AutoboxableVersion) o;
        }
        else
        {
            try
            {
                ov = AutoboxingParser.parseVersion( o.toString(), rebuildIndicator, qualifiers );
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                throw new IllegalArgumentException( "toString() method of " + o.getClass().getName()
                                + " does not render the version string faithfully! Cannot make a comparison!" );
            }
        }

        final VersionPart[] otherParts = ov.parts;
        final int sharedLen = parts.length > otherParts.length ? otherParts.length : parts.length;
        for ( int i = 0; i < sharedLen; i++ )
        {
            final int comp = parts[i].compareTo( otherParts[i] );
            if ( comp != 0 )
            {
                return comp;
            }
        }

        if ( isSnapshot && ov.isSnapshot )
        {
            if ( parts.length == otherParts.length )
            {
                return 0;
            }

            /*
             * Looking at parts[len-1].getType() == LOCAL_SNAPSHOT would be a form of double-checking I think...and I'm
             * not 100% sure we need it.
             * 
             * isSnapshot being equal and parts.length being different, the longer parts.length should sort LOWER.
             * Otherwise, parts.length should not be equal, or else we should never get this far without a result.
             */
            return parts.length < otherParts.length ? 1 : -1;
        }

        if ( parts.length > otherParts.length )
        {
            if ( isSnapshot )
            {
                return -1;
            }
            else
            {
                for ( int i = otherParts.length; i < parts.length; i++ )
                {
                    if ( VersionPartType.compare( INT, parts[i].getType() ) > 0 )
                    {
                        return -1;
                    }
                }
            }

            return 1;
        }
        else if ( parts.length < otherParts.length )
        {
            if ( ov.isSnapshot )
            {
                return 1;
            }
            else
            {
                for ( int i = parts.length; i < otherParts.length; i++ )
                {
                    if ( VersionPartType.compare( INT, otherParts[i].getType() ) > 0 )
                    {
                        return 1;
                    }
                }
            }

            return -1;
        }

        return 0;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        for ( final VersionPart part : parts )
        {
            builder.append( part );
        }

        return builder.toString();
    }

    public VersionPart[] getParts()
    {
        // defensive copy, so this class remains immutable.
        return Arrays.asList( parts ).toArray( new VersionPart[] {} );
    }

    public boolean isSnapshot()
    {
        return isSnapshot;
    }

    public AutoboxableVersion createAutoboxUpperBoundVersion()
    {
        return createRebuildVersion( MAX_REBUILD_NUMBER );
    }

    public AutoboxableVersion createRebuildVersion( final int rebuildNumber )
    {
        final String rebuild = Integer.toString( rebuildNumber );
        return createRebuildVersion( rebuild );
    }

    public AutoboxableVersion createRebuildVersion( final String rebuildNumber )
    {
        final List<VersionPart> newParts = new ArrayList<VersionPart>();
        if ( isRebuild() )
        {
            if ( parts[parts.length - 1].getRawPart().equals( rebuildNumber ) )
            {
                return this;
            }

            for ( int i = 0; i < parts.length - 2; i++ )
            {
                newParts.add( parts[i] );
            }
        }
        else
        {
            newParts.addAll( Arrays.asList( parts ) );
        }

        newParts.add( new VersionPart( REBUILD, rebuildIndicator, DASH, '-', qualifiers ) );
        newParts.add( new VersionPart( INT, rebuildNumber, DASH, '-', qualifiers ) );

        return new AutoboxableVersion( isSnapshot, rebuildIndicator, qualifiers,
                                       newParts.toArray( new VersionPart[] {} ) );
    }

    public boolean isRebuild()
    {
        return parts.length > 2 && parts[parts.length - 2].getType() == REBUILD;
    }
}
