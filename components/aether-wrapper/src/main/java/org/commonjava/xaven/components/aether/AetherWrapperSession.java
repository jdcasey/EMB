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

package org.commonjava.xaven.components.aether;

import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.util.DefaultRepositorySystemSession;

import java.util.ArrayList;
import java.util.List;

public class AetherWrapperSession
{

    private final List<RemoteRepository> remoteRepositories;

    private final DefaultRepositorySystemSession repositorySystemSession;

    public AetherWrapperSession()
    {
        repositorySystemSession = new DefaultRepositorySystemSession();
        remoteRepositories = new ArrayList<RemoteRepository>();
    }

    public AetherWrapperSession( final RepositorySystemSession session )
    {
        repositorySystemSession = new DefaultRepositorySystemSession( session );
        remoteRepositories = new ArrayList<RemoteRepository>();
    }

    public AetherWrapperSession( final AetherWrapperSession session )
    {
        repositorySystemSession = new DefaultRepositorySystemSession( session.repositorySystemSession );
        remoteRepositories = new ArrayList<RemoteRepository>();
        if ( !session.remoteRepositories.isEmpty() )
        {
            for ( final RemoteRepository repo : session.remoteRepositories )
            {
                remoteRepositories.add( new RemoteRepository( repo ) );
            }
        }
    }

    public AetherWrapperSession withRemoteRepository( final RemoteRepository remoteRepository )
    {
        remoteRepositories.add( new RemoteRepository( remoteRepository ) );
        return this;
    }

    public void clearRemoteRepositories()
    {
        remoteRepositories.clear();
    }

    public List<RemoteRepository> getRemoteRepositories()
    {
        return remoteRepositories;
    }

    public DefaultRepositorySystemSession getRepositorySystemSession()
    {
        return repositorySystemSession;
    }

}
