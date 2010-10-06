package org.commonjava.emb.boot.services;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.commonjava.emb.boot.embed.EMBEmbeddingException;
import org.sonatype.aether.RepositorySystemSession;

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

public interface EMBServiceManager
{

    ProjectBuilder projectBuilder();

    RepositorySystem repositorySystem();

    RepositorySystemSession createRepositorySystemSession()
        throws EMBEmbeddingException;

    RepositorySystemSession createRepositorySystemSession( MavenExecutionRequest request );

    <T> T service( Class<T> type )
        throws EMBEmbeddingException;

    <T> T service( Class<T> type, String hint )
        throws EMBEmbeddingException;

    ArtifactRepository defaultLocalRepository()
        throws EMBEmbeddingException;

}
