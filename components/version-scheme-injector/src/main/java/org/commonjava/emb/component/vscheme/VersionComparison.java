package org.commonjava.emb.component.vscheme;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

/**
 * Generic implementation of version comparison. Features:
 * <ul>
 * <li>mixing of '<code>-</code>' (dash) and '<code>.</code>' (dot) separators,</li>
 * <li>transition between characters and digits also constitutes a separator:
 *     <code>1.0alpha1 =&gt; [1, 0, alpha, 1]</code></li>
 * <li>unlimited number of version components,</li>
 * <li>version components in the text can be digits or strings</li>
 * <li>strings are checked for well-known qualifiers and the qualifier ordering is used for version ordering.
 *     Well-known qualifiers (case insensitive):<ul>
 *     <li><code>snapshot</code></li>
 *     <li><code>alpha</code> or <code>a</code></li>
 *     <li><code>beta</code> or <code>b</code></li>
 *     <li><code>milestone</code> or <code>m</code></li>
 *     <li><code>rc</code> or <code>cr</code></li>
 *     <li><code>(the empty string)</code> or <code>ga</code> or <code>final</code></li>
 *     <li><code>sp</code></li>
 *     </ul>
 *   </li>
 * <li>a dash usually precedes a qualifier, and is always less important than something preceded with a dot.</li>
 * </ul>
 *
 * @see <a href="http://docs.codehaus.org/display/MAVEN/Versioning">"Versioning" on Maven Wiki</a>
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 * @author <a href="mailto:hboutemy@apache.org">Hervé Boutemy</a>
 * @version $Id$
 * 
 * Forked from maven 3.0-beta-2
 */
public class VersionComparison
    implements Comparable<VersionComparison>
{

    private static final List<String> _QUALIFIERS;

    private static final Map<String, String> ALIASES;

    static
    {
        final List<String> quals =
            Arrays.asList( new String[] { "alpha", "beta", "milestone", "rc", "snapshot", "", "sp" } );
        _QUALIFIERS = Collections.unmodifiableList( quals );

        final Map<String, String> aliases = new HashMap<String, String>();
        aliases.put( "ga", "" );
        aliases.put( "final", "" );
        aliases.put( "cr", "rc" );

        ALIASES = Collections.unmodifiableMap( aliases );
    }

    private String value;

    private String canonical;

    private ListItem items;

    protected interface Item
    {
        final int INTEGER_ITEM = 0;

        final int STRING_ITEM = 1;

        final int LIST_ITEM = 2;

        int compareTo( Item item );

        int getType();

        boolean isNull();
    }

    /**
     * Represents a numeric item in the version item list.
     */
    protected static class IntegerItem
        implements Item
    {
        private static final BigInteger BigInteger_ZERO = new BigInteger( "0" );

        private final BigInteger value;

        public static final IntegerItem ZERO = new IntegerItem();

        private IntegerItem()
        {
            value = BigInteger_ZERO;
        }

        public IntegerItem( final String str )
        {
            value = new BigInteger( str );
        }

        public int getType()
        {
            return INTEGER_ITEM;
        }

        public boolean isNull()
        {
            return BigInteger_ZERO.equals( value );
        }

        public int compareTo( final Item item )
        {
            if ( item == null )
            {
                return BigInteger_ZERO.equals( value ) ? 0 : 1; // 1.0 == 1, 1.1 > 1
            }

            switch ( item.getType() )
            {
                case INTEGER_ITEM:
                    return value.compareTo( ( (IntegerItem) item ).value );

                case STRING_ITEM:
                    return 1; // 1.1 > 1-sp

                case LIST_ITEM:
                    return 1; // 1.1 > 1-1

                default:
                    throw new RuntimeException( "invalid item: " + item.getClass() );
            }
        }

        @Override
        public String toString()
        {
            return value.toString();
        }
    }

    /**
     * Represents a string in the version item list, usually a qualifier.
     */
    protected static class StringItem
        implements Item
    {
        private final String value;

        private final List<String> stringQualifiers;

        private final String releaseVersionIndex;

        public StringItem( String value, final boolean followedByDigit, final List<String> stringQualifiers,
                           final Map<String, String> stringAliases )
        {
            this.stringQualifiers = stringQualifiers;

            if ( followedByDigit && value.length() == 1 )
            {
                // a1 = alpha-1, b1 = beta-1, m1 = milestone-1
                switch ( value.charAt( 0 ) )
                {
                    case 'a':
                        value = "alpha";
                        break;
                    case 'b':
                        value = "beta";
                        break;
                    case 'm':
                        value = "milestone";
                        break;
                }
            }

            final String val = stringAliases.get( value );
            this.value = val == null ? value : val;

            releaseVersionIndex = String.valueOf( stringQualifiers.indexOf( "" ) );
        }

        public int getType()
        {
            return STRING_ITEM;
        }

        public boolean isNull()
        {
            return ( comparableQualifier( value ).compareTo( releaseVersionIndex ) == 0 );
        }

        /**
         * Returns a comparable value for a qualifier.
         *
         * This method both takes into account the ordering of known qualifiers as well as lexical ordering for unknown
         * qualifiers.
         *
         * just returning an Integer with the index here is faster, but requires a lot of if/then/else to check for -1
         * or QUALIFIERS.size and then resort to lexical ordering. Most comparisons are decided by the first character,
         * so this is still fast. If more characters are needed then it requires a lexical sort anyway.
         *
         * @param qualifier
         * @return an equivalent value that can be used with lexical comparison
         */
        public String comparableQualifier( final String qualifier )
        {
            final int i = stringQualifiers.indexOf( qualifier );

            return i == -1 ? stringQualifiers.size() + "-" + qualifier : String.valueOf( i );
        }

        public int compareTo( final Item item )
        {
            if ( item == null )
            {
                // 1-rc < 1, 1-ga > 1
                return comparableQualifier( value ).compareTo( releaseVersionIndex );
            }
            switch ( item.getType() )
            {
                case INTEGER_ITEM:
                    return -1; // 1.any < 1.1 ?

                case STRING_ITEM:
                    return comparableQualifier( value ).compareTo( comparableQualifier( ( (StringItem) item ).value ) );

                case LIST_ITEM:
                    return -1; // 1.any < 1-1

                default:
                    throw new RuntimeException( "invalid item: " + item.getClass() );
            }
        }

        @Override
        public String toString()
        {
            return value;
        }
    }

    /**
     * Represents a version list item. This class is used both for the global item list and for sub-lists (which start
     * with '-(number)' in the version specification).
     */
    protected static class ListItem
        extends ArrayList<Item>
        implements Item
    {
        private static final long serialVersionUID = 1L;

        public int getType()
        {
            return LIST_ITEM;
        }

        public boolean isNull()
        {
            return ( size() == 0 );
        }

        void normalize()
        {
            for ( final ListIterator<Item> iterator = listIterator( size() ); iterator.hasPrevious(); )
            {
                final Item item = iterator.previous();
                if ( item.isNull() )
                {
                    iterator.remove(); // remove null trailing items: 0, "", empty list
                }
                else
                {
                    break;
                }
            }
        }

        public int compareTo( final Item item )
        {
            if ( item == null )
            {
                if ( size() == 0 )
                {
                    return 0; // 1-0 = 1- (normalize) = 1
                }
                final Item first = get( 0 );
                return first.compareTo( null );
            }
            switch ( item.getType() )
            {
                case INTEGER_ITEM:
                    return -1; // 1-1 < 1.0.x

                case STRING_ITEM:
                    return 1; // 1-1 > 1-sp

                case LIST_ITEM:
                    final Iterator<Item> left = iterator();
                    final Iterator<Item> right = ( (ListItem) item ).iterator();

                    while ( left.hasNext() || right.hasNext() )
                    {
                        final Item l = left.hasNext() ? left.next() : null;
                        final Item r = right.hasNext() ? right.next() : null;

                        // if this is shorter, then invert the compare and mul with -1
                        final int result = l == null ? -1 * r.compareTo( l ) : l.compareTo( r );

                        if ( result != 0 )
                        {
                            return result;
                        }
                    }

                    return 0;

                default:
                    throw new RuntimeException( "invalid item: " + item.getClass() );
            }
        }

        @Override
        public String toString()
        {
            final StringBuilder buffer = new StringBuilder( "(" );
            for ( final Iterator<Item> iter = iterator(); iter.hasNext(); )
            {
                buffer.append( iter.next() );
                if ( iter.hasNext() )
                {
                    buffer.append( ',' );
                }
            }
            buffer.append( ')' );
            return buffer.toString();
        }
    }

    protected VersionComparison()
    {
    }

    public VersionComparison( final String version )
    {
        parseVersion( version );
    }

    protected void parseVersion( String version )
    {
        value = version;

        items = new ListItem();

        version = version.toLowerCase( Locale.ENGLISH );

        ListItem list = items;

        final Stack<Item> stack = new Stack<Item>();
        stack.push( list );

        boolean isDigit = false;

        int startIndex = 0;

        for ( int i = 0; i < version.length(); i++ )
        {
            final char c = version.charAt( i );

            if ( c == '.' )
            {
                if ( i == startIndex )
                {
                    list.add( IntegerItem.ZERO );
                }
                else
                {
                    list.add( parseItem( isDigit, version.substring( startIndex, i ) ) );
                }
                startIndex = i + 1;
            }
            else if ( c == '-' )
            {
                if ( i == startIndex )
                {
                    list.add( IntegerItem.ZERO );
                }
                else
                {
                    list.add( parseItem( isDigit, version.substring( startIndex, i ) ) );
                }
                startIndex = i + 1;

                if ( isDigit )
                {
                    list.normalize(); // 1.0-* = 1-*

                    if ( ( i + 1 < version.length() ) && Character.isDigit( version.charAt( i + 1 ) ) )
                    {
                        // new ListItem only if previous were digits and new char is a digit,
                        // ie need to differentiate only 1.1 from 1-1
                        list.add( list = new ListItem() );

                        stack.push( list );
                    }
                }
            }
            else if ( Character.isDigit( c ) )
            {
                if ( !isDigit && i > startIndex )
                {
                    list.add( new StringItem( version.substring( startIndex, i ), true, getStringQualifiers(),
                                              getStringAliases() ) );
                    startIndex = i;
                }

                isDigit = true;
            }
            else
            {
                if ( isDigit && i > startIndex )
                {
                    list.add( parseItem( true, version.substring( startIndex, i ) ) );
                    startIndex = i;
                }

                isDigit = false;
            }
        }

        if ( version.length() > startIndex )
        {
            list.add( parseItem( isDigit, version.substring( startIndex ) ) );
        }

        while ( !stack.isEmpty() )
        {
            list = (ListItem) stack.pop();
            list.normalize();
        }

        canonical = items.toString();
    }

    private Item parseItem( final boolean isDigit, final String buf )
    {
        return isDigit ? new IntegerItem( buf )
                        : new StringItem( buf, false, getStringQualifiers(), getStringAliases() );
    }

    public int compareTo( final VersionComparison o )
    {
        return items.compareTo( o.items );
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public boolean equals( final Object o )
    {
        return ( o instanceof VersionComparison ) && canonical.equals( ( (VersionComparison) o ).canonical );
    }

    @Override
    public int hashCode()
    {
        return canonical.hashCode();
    }

    protected Map<String, String> getStringAliases()
    {
        return getBaseAliases();
    }

    protected List<String> getStringQualifiers()
    {
        return getBaseQualifiers();
    }

    protected static Map<String, String> getBaseAliases()
    {
        return ALIASES;
    }

    protected static List<String> getBaseQualifiers()
    {
        return _QUALIFIERS;
    }
}
