package org.commonjava.emb.boot.services;

import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulationException;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.commonjava.emb.DefaultEMBExecutionRequest;
import org.commonjava.emb.boot.embed.EMBEmbeddingException;
import org.commonjava.emb.plexus.ServiceAuthorizer;
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

@Component( role = EMBServiceManager.class )
public class DefaultEMBServiceManager
    implements EMBServiceManager, Contextualizable
{

    // private final Logger logger = Logger.getLogger( EMBConfiguration.STANDARD_LOG_HANDLE_CORE );

    @Requirement
    private ProjectBuilder projectBuilder;

    @Requirement
    private RepositorySystem repositorySystem;

    @Requirement
    private ServiceAuthorizer authorizer;

    @Requirement( role = Maven.class, hint = "default_" )
    private DefaultMaven defaultMaven;

    @Requirement
    private MavenExecutionRequestPopulator requestPopulator;

    private transient ArtifactRepository defaultLocalRepo;

    private PlexusContainer container;

    public DefaultEMBServiceManager()
    {
    }

    public DefaultEMBServiceManager( final PlexusContainer container, final ProjectBuilder projectBuilder,
                                     final RepositorySystem repositorySystem )
    {
        this.container = container;
        this.projectBuilder = projectBuilder;
        this.repositorySystem = repositorySystem;
    }

    public ProjectBuilder projectBuilder()
    {
        return projectBuilder;
    }

    public RepositorySystem repositorySystem()
    {
        return repositorySystem;
    }

    public RepositorySystemSession createRepositorySystemSession()
        throws EMBEmbeddingException
    {
        try
        {
            final MavenExecutionRequest req =
                requestPopulator.populateDefaults( new DefaultEMBExecutionRequest().asMavenExecutionRequest() );

            return defaultMaven.newRepositorySession( req );
        }
        catch ( final MavenExecutionRequestPopulationException e )
        {
            throw new EMBEmbeddingException( "Failed to populate default Maven execution request, "
                            + " for use in constructing a repository system session." + "\nReason: %s", e,
                                             e.getMessage() );
        }
    }

    public RepositorySystemSession createRepositorySystemSession( final MavenExecutionRequest request )
    {
        return defaultMaven.newRepositorySession( request );
    }

    public synchronized ArtifactRepository defaultLocalRepository()
        throws EMBEmbeddingException
    {
        if ( defaultLocalRepo == null )
        {
            try
            {
                defaultLocalRepo = repositorySystem().createDefaultLocalRepository();
            }
            catch ( final InvalidRepositoryException e )
            {
                throw new EMBEmbeddingException( "Failed to create default local-repository instance: {0}", e,
                                                 e.getMessage() );
            }
        }

        return defaultLocalRepo;
    }

    @Override
    public <T> T service( final Class<T> type )
        throws EMBEmbeddingException
    {
        if ( type == null )
        {
            throw new EMBEmbeddingException( "Invalid service: null" );
        }

        if ( !authorizer.isAvailable( type ) )
        {
            throw new UnauthorizedServiceException( type );
        }

        try
        {
            return type.cast( container.lookup( type ) );
        }
        catch ( final ComponentLookupException e )
        {
            throw new EMBEmbeddingException( "Failed to retrieve service: %s. Reason: %s", e, type.getName(),
                                             e.getMessage() );
        }
    }

    @Override
    public <T> T service( final Class<T> type, final String hint )
        throws EMBEmbeddingException
    {
        if ( type == null )
        {
            throw new EMBEmbeddingException( "Invalid service: null" );
        }

        if ( !authorizer.isAvailable( type, hint ) )
        {
            throw new UnauthorizedServiceException( type, hint == null ? "" : hint );
        }

        try
        {
            return type.cast( container.lookup( type, hint ) );
        }
        catch ( final ComponentLookupException e )
        {
            throw new EMBEmbeddingException( "Failed to retrieve service: %s with hint: %s. Reason: %s", e,
                                             type.getName(), hint, e.getMessage() );
        }
    }

    @Override
    public void contextualize( final Context ctx )
        throws ContextException
    {
        container = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

}
