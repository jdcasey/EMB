package org.commonjava.xaven.boot.m3.services;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.xaven.boot.m3.embed.XavenEmbeddingException;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

@Component( role = XavenServiceManager.class, hint = "default" )
public class DefaultXavenServiceManager
    implements XavenServiceManager
{

    @Requirement
    private ProjectBuilder projectBuilder;

    @Requirement
    private RepositorySystem repositorySystem;

    private transient ArtifactRepository defaultLocalRepo;

    public ProjectBuilder projectBuilder()
    {
        return projectBuilder;
    }

    public RepositorySystem repositorySystem()
    {
        return repositorySystem;
    }

    public synchronized ArtifactRepository defaultLocalRepository()
        throws XavenEmbeddingException
    {
        if ( defaultLocalRepo == null )
        {
            try
            {
                defaultLocalRepo = repositorySystem().createDefaultLocalRepository();
            }
            catch ( final InvalidRepositoryException e )
            {
                throw new XavenEmbeddingException( "Failed to create default local-repository instance: {0}", e,
                                                   e.getMessage() );
            }
        }

        return defaultLocalRepo;
    }

}
