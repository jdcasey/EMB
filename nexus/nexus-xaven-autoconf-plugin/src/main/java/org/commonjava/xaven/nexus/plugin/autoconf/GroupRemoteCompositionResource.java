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

import org.commonjava.xaven.nexus.plugin.autoconf.proto.GroupRemoteUrlResponse;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import javax.inject.Named;

import java.util.ArrayList;
import java.util.List;

@Named( "groupRemoteComposition" )
public class GroupRemoteCompositionResource
    extends AbstractNexusPlexusResource
{

    private static final String REPOSITORY_ID = "repositoryId";

    private static final Logger logger = LoggerFactory.getLogger( GroupRemoteCompositionResource.class.getName() );

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/group-info/*/remote-urls", "authcBasic" );
    }

    @Override
    public String getResourceUri()
    {
        return "/group-info/{" + REPOSITORY_ID + "}/remote-urls";
    }

    @Override
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        final String repoId = requestAttribute( REPOSITORY_ID, request );

        GroupRemoteUrlResponse result = null;
        try
        {
            final GroupRepository groupRepository =
                getRepositoryRegistry().getRepositoryWithFacet( repoId, GroupRepository.class );

            final List<String> memberIds = groupRepository.getMemberRepositoryIds();
            final List<String> memberUrls = new ArrayList<String>( memberIds == null ? 0 : memberIds.size() );

            if ( memberIds != null && !memberIds.isEmpty() )
            {
                for ( final String memberId : memberIds )
                {
                    final ProxyRepository member =
                        getRepositoryRegistry().getRepositoryWithFacet( memberId, ProxyRepository.class );
                    memberUrls.add( member.getRemoteUrl() );
                }
            }

            result = new GroupRemoteUrlResponse( groupRepository.getId(), groupRepository.getLocalUrl(), memberUrls );
        }
        catch ( final NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Not a repository group: " + repoId );
        }

        return result;
    }

    public static String requestAttribute( final String param, final Request request )
        throws ResourceException
    {
        final Object val = request.getAttributes().get( param );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( String.format( "%s attribute value: %s", param, val ) );
        }

        final String value = val == null ? null : val.toString();
        if ( value == null || value.trim().length() < 1 )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Request path attribute not found: " + param );
        }

        return value;
    }
}
