/*
 * Copyright 2011 Red Hat, Inc.
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

package org.commonjava.emb.project.session;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.mae.MAEException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.commonjava.emb.project.ProjectToolsException;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.util.List;

/**
 * 
 */
public interface SessionInjector
{

    ProjectBuildingRequest getProjectBuildingRequest( final ProjectToolsSession session )
        throws ProjectToolsException;

    RepositorySystemSession getRepositorySystemSession( final ProjectToolsSession session )
        throws MAEException;

    List<RemoteRepository> getRemoteRepositories( final ProjectToolsSession session )
        throws ProjectToolsException;

    List<ArtifactRepository> getArtifactRepositories( final ProjectToolsSession session )
        throws ProjectToolsException;

}
