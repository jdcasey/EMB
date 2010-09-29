package org.commonjava.emb.boot.services;

import org.apache.log4j.Logger;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
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
import org.commonjava.emb.boot.embed.EMBEmbeddingException;
import org.commonjava.emb.conf.EMBConfiguration;
import org.commonjava.emb.plexus.ServiceAuthorizer;

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

    private final Logger logger = Logger.getLogger( EMBConfiguration.STANDARD_LOG_HANDLE_CORE );

    @Requirement
    private ProjectBuilder projectBuilder;

    @Requirement
    private RepositorySystem repositorySystem;

    @Requirement
    private ServiceAuthorizer authorizer;

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
        logger.info( "Returning project-builder instance from service manager: " + projectBuilder );
        return projectBuilder;
    }

    public RepositorySystem repositorySystem()
    {
        logger.info( "Returning repository-system instance from service manager: " + repositorySystem );
        return repositorySystem;
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
            throw new UnauthorizedServiceException( type, hint );
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
