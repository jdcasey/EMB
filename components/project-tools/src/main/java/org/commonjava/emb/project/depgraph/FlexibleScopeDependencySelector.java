package org.commonjava.emb.project.depgraph;

/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/

import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A dependency selector that filters transitive dependencies based on their scope. Direct dependencies are always
 * included regardless of their scope. <em>Note:</em> This filter does not assume any relationships between the scopes.
 * In particular, the filter is not aware of scopes that logically include other scopes.
 * 
 * @author Benjamin Bentmann
 * @see Dependency#getScope()
 */
public class FlexibleScopeDependencySelector
    implements DependencySelector
{

    private boolean transitive;

    private final Set<String> included;

    private final Set<String> excluded;
    
    public FlexibleScopeDependencySelector()
    {
        included = new HashSet<String>();
        excluded = new HashSet<String>();
    }
    
    /**
     * Creates a new selector using the specified includes and excludes.
     * 
     * @param included The set of scopes to include, may be {@code null} or empty to include any scope.
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public FlexibleScopeDependencySelector( Collection<String> included, Collection<String> excluded )
    {
        transitive = false;
        if ( included != null )
        {
            this.included = new HashSet<String>();
            this.included.addAll( included );
        }
        else
        {
            this.included = Collections.emptySet();
        }
        if ( excluded != null )
        {
            this.excluded = new HashSet<String>();
            this.excluded.addAll( excluded );
        }
        else
        {
            this.excluded = Collections.emptySet();
        }
    }

    /**
     * Creates a new selector using the specified excludes.
     * 
     * @param excluded The set of scopes to exclude, may be {@code null} or empty to exclude no scope.
     */
    public FlexibleScopeDependencySelector( String... excluded )
    {
        this( null, Arrays.asList( excluded ) );
    }

    private FlexibleScopeDependencySelector( boolean transitive, Collection<String> included, Collection<String> excluded )
    {
        this.transitive = transitive;
        this.included = included == null ? null : new HashSet<String>( included );
        this.excluded = excluded == null ? null : new HashSet<String>( excluded );
    }

    public boolean selectDependency( Dependency dependency )
    {
        if ( !transitive )
        {
            return true;
        }

        String scope = dependency.getScope();
        return ( included.isEmpty() || included.contains( scope ) )
            && ( excluded.isEmpty() || !excluded.contains( scope ) );
    }

    public DependencySelector deriveChildSelector( DependencyCollectionContext context )
    {
        if ( this.transitive || context.getDependency() == null )
        {
            return this;
        }

        return new FlexibleScopeDependencySelector( true, included, excluded );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        else if ( null == obj || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }

        FlexibleScopeDependencySelector that = (FlexibleScopeDependencySelector) obj;
        return transitive == that.transitive && included.equals( that.included ) && excluded.equals( that.excluded );
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + ( transitive ? 1 : 0 );
        hash = hash * 31 + included.hashCode();
        hash = hash * 31 + excluded.hashCode();
        return hash;
    }

    public FlexibleScopeDependencySelector includeScope( String scope )
    {
        included.add( scope );
        return this;
    }

    public FlexibleScopeDependencySelector excludeScope( String scope )
    {
        excluded.add( scope );
        return this;
    }
    
    public FlexibleScopeDependencySelector setTransitive( boolean transitive )
    {
        this.transitive = transitive;
        return this;
    }
}
