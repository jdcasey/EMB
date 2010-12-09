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

package org.commonjava.emb.event.resolver;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.emb.event.EMBEventManager;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;

import java.util.List;

@Component( role = PluginDependenciesResolver.class, hint = "emb-events" )
public class EventingPluginDependenciesResolver
    implements PluginDependenciesResolver
{

    @Requirement
    private EMBEventManager eventManager;

    @Requirement( hint = "#" )
    protected PluginDependenciesResolver delegate;

    public Artifact resolve( final Plugin plugin, final List<RemoteRepository> repositories,
                             final RepositorySystemSession session )
        throws PluginResolutionException
    {
        eventManager.fireEvent( new PluginResolutionEvent( plugin, repositories, session ) );

        try
        {
            final Artifact pluginArtifact = delegate.resolve( plugin, repositories, session );

            eventManager.fireEvent( new PluginResolutionEvent( plugin, repositories, session, pluginArtifact ) );

            return pluginArtifact;
        }
        catch ( final PluginResolutionException e )
        {
            eventManager.fireEvent( new PluginResolutionEvent( plugin, repositories, session, e ) );
            throw e;
        }
    }

    @Override
    public DependencyNode resolve( final Plugin plugin, final Artifact pluginArtifact,
                                   final DependencyFilter dependencyFilter, final List<RemoteRepository> repositories,
                                   final RepositorySystemSession session )
        throws PluginResolutionException
    {
        eventManager.fireEvent( new PluginResolutionEvent( plugin, pluginArtifact, repositories, session,
                                                           dependencyFilter ) );

        try
        {
            final DependencyNode result =
                delegate.resolve( plugin, pluginArtifact, dependencyFilter, repositories, session );

            eventManager.fireEvent( new PluginResolutionEvent( plugin, pluginArtifact, repositories, session,
                                                               dependencyFilter, result ) );

            return result;
        }
        catch ( final PluginResolutionException e )
        {
            eventManager.fireEvent( new PluginResolutionEvent( plugin, pluginArtifact, repositories, session,
                                                               dependencyFilter, e ) );
            throw e;
        }
    }

}
