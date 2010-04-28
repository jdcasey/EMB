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

package org.commonjava.xaven.event.resolver;

import org.apache.maven.ProjectDependenciesResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.xaven.event.XavenEventManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Component( role = ProjectDependenciesResolver.class, hint = "xaven-events" )
public class EventingProjectDependenciesResolver
    implements ProjectDependenciesResolver
{

    private static final Set<Artifact> EMPTY_IGNORES = Collections.emptySet();

    private static final Set<String> EMPTY_SCOPES = Collections.emptySet();

    @Requirement
    private XavenEventManager eventManager;

    @Requirement( hint = "#" )
    private ProjectDependenciesResolver delegate;

    public Set<Artifact> resolve( final Collection<? extends MavenProject> projects,
                                  final Collection<String> scopesToResolve, final MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                 scopesToResolve, session,
                                                                                 EMPTY_IGNORES ) );
        try
        {
            final Set<Artifact> result = delegate.resolve( projects, scopesToResolve, session );

            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, result ) );

            return result;
        }
        catch ( final ArtifactResolutionException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, e ) );
            throw e;
        }
        catch ( final ArtifactNotFoundException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, e ) );
            throw e;
        }
    }

    public Set<Artifact> resolve( final MavenProject project, final Collection<String> scopesToCollect,
                                  final Collection<String> scopesToResolve, final MavenSession session,
                                  final Set<Artifact> ignoreableArtifacts )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        final Collection<MavenProject> projects = Collections.singleton( project );
        eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                 scopesToResolve, session,
                                                                                 ignoreableArtifacts ) );
        try
        {
            final Set<Artifact> result =
                delegate.resolve( project, scopesToCollect, scopesToResolve, session, ignoreableArtifacts );

            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                     scopesToResolve, session,
                                                                                     ignoreableArtifacts, result ) );

            return result;
        }
        catch ( final ArtifactResolutionException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                     scopesToResolve, session,
                                                                                     ignoreableArtifacts, e ) );
            throw e;
        }
        catch ( final ArtifactNotFoundException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                     scopesToResolve, session,
                                                                                     ignoreableArtifacts, e ) );
            throw e;
        }
    }

    public Set<Artifact> resolve( final MavenProject project, final Collection<String> scopesToCollect,
                                  final Collection<String> scopesToResolve, final MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        final Collection<MavenProject> projects = Collections.singleton( project );
        eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                 scopesToResolve, session,
                                                                                 EMPTY_IGNORES ) );
        try
        {
            final Set<Artifact> result = delegate.resolve( project, scopesToCollect, scopesToResolve, session );

            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, result ) );

            return result;
        }
        catch ( final ArtifactResolutionException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, e ) );
            throw e;
        }
        catch ( final ArtifactNotFoundException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, scopesToCollect,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, e ) );
            throw e;
        }
    }

    public Set<Artifact> resolve( final MavenProject project, final Collection<String> scopesToResolve,
                                  final MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        final Collection<MavenProject> projects = Collections.singleton( project );
        eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                 scopesToResolve, session,
                                                                                 EMPTY_IGNORES ) );
        try
        {
            final Set<Artifact> result = delegate.resolve( project, scopesToResolve, session );

            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, result ) );

            return result;
        }
        catch ( final ArtifactResolutionException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, e ) );
            throw e;
        }
        catch ( final ArtifactNotFoundException e )
        {
            eventManager.fireEvent( new ProjectDependencyResolutionEvent( projects, EMPTY_SCOPES,
                                                                                     scopesToResolve, session,
                                                                                     EMPTY_IGNORES, e ) );
            throw e;
        }
    }

}
