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

package org.commonjava.emb.depgraph.impl;

import org.apache.log4j.Logger;
import org.apache.maven.mae.MAEException;
import org.apache.maven.mae.project.ProjectLoader;
import org.apache.maven.mae.project.session.ProjectToolsSession;
import org.apache.maven.mae.project.session.SessionInjector;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.depgraph.DepGraphLoader;
import org.commonjava.emb.depgraph.DependencyGraph;
import org.sonatype.aether.RepositorySystemSession;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Component( role = DepGraphLoader.class )
public class DefaultDepGraphLoader
    implements DepGraphLoader
{

    @SuppressWarnings( "unused" )
    private static final Logger LOGGER = Logger.getLogger( DefaultDepGraphLoader.class );

    @Requirement
    private DependencyGraphResolver dependencyGraphResolver;

    @Requirement
    private SessionInjector sessionInjector;
    
    @Requirement
    private ProjectLoader projectLoader;

    @Override
    public DependencyGraph loadProjectDependencyGraph( final File rootPom, final ProjectToolsSession session,
                                                       final boolean includeModuleProjects )
        throws MAEException
    {
        List<MavenProject> projects;
        if ( includeModuleProjects )
        {
            projects = projectLoader.buildReactorProjectInstances( session, includeModuleProjects, rootPom );
        }
        else
        {
            projects = Collections.singletonList( projectLoader.buildProjectInstance( rootPom, session ) );
        }

        final DependencyGraph depGraph =
            dependencyGraphResolver.accumulateGraph( projects,
                                                     sessionInjector.getRepositorySystemSession( session ),
                                                     session );
        session.setState( depGraph );

        return depGraph;
    }

    @Override
    public DependencyGraph resolveProjectDependencies( final File rootPom, final ProjectToolsSession session,
                                                       final boolean includeModuleProjects )
        throws MAEException
    {
        List<MavenProject> projects;
        if ( includeModuleProjects )
        {
            projects = projectLoader.buildReactorProjectInstances( session, includeModuleProjects, rootPom );
        }
        else
        {
            projects = Collections.singletonList( projectLoader.buildProjectInstance( rootPom, session ) );
        }

        final RepositorySystemSession rss = sessionInjector.getRepositorySystemSession( session );
        final DependencyGraph depGraph = dependencyGraphResolver.accumulateGraph( projects, rss, session );
        dependencyGraphResolver.resolveGraph( depGraph, projects, rss, session );

        session.setState( depGraph );

        return depGraph;
    }

}
