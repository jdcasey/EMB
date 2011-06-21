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

package org.commonjava.emb.depgraph.impl.session;

import org.apache.maven.mae.project.session.SimpleProjectToolsSession;
import org.apache.maven.model.Repository;
import org.commonjava.emb.depgraph.DependencyGraph;
import org.sonatype.aether.artifact.Artifact;

import java.io.File;

public class DepGraphProjectToolsSession
    extends SimpleProjectToolsSession
{

    public DepGraphProjectToolsSession( File localRepositoryDirectory, Repository... resolveRepositories )
    {
        super( localRepositoryDirectory, resolveRepositories );
    }

    @Override
    public synchronized void connectProjectHierarchy( Artifact parent, boolean parentPreResolved, Artifact child,
                                         boolean childPreResolved )
    {
        DependencyGraph graph = getState( DependencyGraph.class );
        if ( graph == null )
        {
            graph = new DependencyGraph();
            setState( graph );
        }
        
        graph.addDependency( parent, child, parentPreResolved, childPreResolved );
    }

}
