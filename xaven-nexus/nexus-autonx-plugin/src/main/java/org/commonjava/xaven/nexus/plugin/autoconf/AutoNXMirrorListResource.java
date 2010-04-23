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

package org.commonjava.xaven.nexus.plugin.autoconf;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import javax.inject.Named;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named( "autoNXMirrorList" )
public class AutoNXMirrorListResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

    private static final Logger logger = LoggerFactory.getLogger( AutoNXMirrorListResource.class );

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/autonx/mirrors", "authcBasic,perms[nexus:autonx-mirrors]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/autonx/mirrors";
    }

    @Override
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "\n\nGrabbing all M2 repositories..." );
        }

        final List<M2Repository> repos =
            getUnprotectedRepositoryRegistry().getRepositoriesWithFacet( M2Repository.class );

        final List<GroupRepository> groups = getRepositoryRegistry().getRepositoriesWithFacet( GroupRepository.class );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Found " + ( repos == null ? "NO" : repos.size() ) + " M2 repositories." );
            logger.debug( "Found " + ( groups == null ? "NO" : groups.size() ) + " group repositories." );
        }

        final Map<String, String> mappings = new HashMap<String, String>();

        if ( ( repos == null || repos.isEmpty() ) && ( groups == null || groups.isEmpty() ) )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Return 404." );
            }
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
        }

        if ( groups != null )
        {
            for ( final GroupRepository repo : groups )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Processing group repository: " + repo.getId() );
                }

                final String url = getLocalUrl( request, repo );

                final List<Repository> members = repo.getMemberRepositories();
                if ( members != null && !members.isEmpty() )
                {
                    for ( final Repository r : members )
                    {
                        if ( !( r instanceof M2Repository ) )
                        {
                            if ( logger.isDebugEnabled() )
                            {
                                logger.debug( "Skipping member: " + r.getId() );
                            }
                            continue;
                        }

                        if ( logger.isDebugEnabled() )
                        {
                            logger.debug( "Adding member: " + r.getId() );
                        }

                        final M2Repository mr = (M2Repository) r;
                        if ( mr.getRemoteUrl() == null )
                        {
                            continue;
                        }

                        if ( !mappings.containsKey( mr.getRemoteUrl() ) )
                        {
                            mappings.put( mr.getRemoteUrl(), url );
                        }
                    }
                }
            }
        }

        if ( repos != null )
        {
            for ( final M2Repository repo : repos )
            {
                if ( repo.getRemoteUrl() == null )
                {
                    continue;
                }

                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Processing M2 repository: " + repo.getId() );
                }

                if ( !mappings.containsKey( repo.getRemoteUrl() ) )
                {
                    mappings.put( repo.getRemoteUrl(), getLocalUrl( request, repo ) );
                }
            }
        }

        final StringBuilder listing = new StringBuilder();
        for ( final Map.Entry<String, String> mapping : mappings.entrySet() )
        {
            listing.append( mapping.getKey() ).append( '=' ).append( mapping.getValue() ).append( '\n' );
        }

        return new StringRepresentation( listing.toString(), variant == null ? MediaType.TEXT_PLAIN
                        : variant.getMediaType() );
    }

    private String getLocalUrl( final Request request, final Repository repo )
    {
        final StringBuilder builder = new StringBuilder();
        final String root = request.getRootRef().toString();

        builder.append( root );
        if ( !root.endsWith( "/" ) )
        {
            builder.append( '/' );
        }

        builder.append( "content/repositories/" ).append( repo.getId() );

        return builder.toString();
    }

    @Override
    public List<Variant> getVariants()
    {
        return Collections.singletonList( new Variant( MediaType.TEXT_PLAIN ) );
    }
}
