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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.commonjava.xaven.event.XavenEvent;

import java.util.Collection;
import java.util.Set;

public class ProjectDependencyResolutionEvent
    extends XavenEvent
    implements ResolutionEvent
{

    private final Collection<? extends MavenProject> projects;

    private final Collection<String> scopesToCollect;

    private final MavenSession session;

    private final ResolutionEventType type;

    private final Set<Artifact> result;

    private final AbstractArtifactResolutionException error;

    private final Collection<String> scopesToResolve;

    private final Set<Artifact> ignoreableArtifacts;

    public ProjectDependencyResolutionEvent( final Collection<? extends MavenProject> projects,
                                             final Collection<String> scopesToCollect,
                                             final Collection<String> scopesToResolve, final MavenSession session,
                                             final Set<Artifact> ignoreableArtifacts )
    {
        this.projects = projects;
        this.scopesToCollect = scopesToCollect;
        this.scopesToResolve = scopesToResolve;
        this.session = session;
        this.ignoreableArtifacts = ignoreableArtifacts;
        type = ResolutionEventType.START;
        result = null;
        error = null;
    }

    public ProjectDependencyResolutionEvent( final Collection<? extends MavenProject> projects,
                                             final Collection<String> scopesToCollect,
                                             final Collection<String> scopesToResolve, final MavenSession session,
                                             final Set<Artifact> ignoreableArtifacts, final Set<Artifact> result )
    {
        this.projects = projects;
        this.scopesToCollect = scopesToCollect;
        this.scopesToResolve = scopesToResolve;
        this.session = session;
        this.result = result;
        this.ignoreableArtifacts = ignoreableArtifacts;
        type = ResolutionEventType.SUCCESS;
        error = null;
    }

    public ProjectDependencyResolutionEvent( final Collection<? extends MavenProject> projects,
                                             final Collection<String> scopesToCollect,
                                             final Collection<String> scopesToResolve, final MavenSession session,
                                             final Set<Artifact> ignoreableArtifacts,
                                             final AbstractArtifactResolutionException error )
    {
        this.projects = projects;
        this.scopesToCollect = scopesToCollect;
        this.scopesToResolve = scopesToResolve;
        this.session = session;
        this.error = error;
        this.ignoreableArtifacts = ignoreableArtifacts;
        type = ResolutionEventType.FAIL;
        result = null;
    }

    public Collection<? extends MavenProject> getProjects()
    {
        return projects;
    }

    public Collection<String> getScopesToCollect()
    {
        return scopesToCollect;
    }

    public MavenSession getSession()
    {
        return session;
    }

    public ResolutionEventType getType()
    {
        return type;
    }

    public Set<Artifact> getResolvedArtifacts()
    {
        return result;
    }

    public AbstractArtifactResolutionException getError()
    {
        return error;
    }

    public Collection<String> getScopesToResolve()
    {
        return scopesToResolve;
    }

    public Set<Artifact> getIgnoreableArtifacts()
    {
        return ignoreableArtifacts;
    }

}
