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

package org.commonjava.xaven.event.resolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.commonjava.xaven.event.XavenEventManager;

import java.util.List;

@Component( role = PluginDependenciesResolver.class, hint = "xaven-events" )
public class EventingPluginDependenciesResolver
    implements PluginDependenciesResolver
{

    @Requirement
    private XavenEventManager eventManager;

    @Requirement( hint = "#" )
    protected PluginDependenciesResolver delegate;

    public Artifact resolve( final Plugin plugin, final ArtifactResolutionRequest request )
        throws PluginResolutionException
    {
        eventManager.fireEvent( new PluginResolutionEvent( plugin, request ) );

        try
        {
            final Artifact pluginArtifact = delegate.resolve( plugin, request );

            eventManager.fireEvent( new PluginResolutionEvent( plugin, request, pluginArtifact ) );

            return pluginArtifact;
        }
        catch ( final PluginResolutionException e )
        {
            eventManager.fireEvent( new PluginResolutionEvent( plugin, request, e ) );
            throw e;
        }
    }

    public List<Artifact> resolve( final Plugin plugin, final Artifact pluginArtifact,
                                   final ArtifactResolutionRequest request, final ArtifactFilter dependencyFilter )
        throws PluginResolutionException
    {
        eventManager.fireEvent( new PluginResolutionEvent( plugin, pluginArtifact, request, dependencyFilter ) );

        try
        {
            final List<Artifact> pluginArtifacts = delegate.resolve( plugin, pluginArtifact, request, dependencyFilter );

            eventManager.fireEvent( new PluginResolutionEvent( plugin, pluginArtifact, request,
                                                                          dependencyFilter, pluginArtifacts ) );

            return pluginArtifacts;
        }
        catch ( final PluginResolutionException e )
        {
            eventManager.fireEvent( new PluginResolutionEvent( plugin, pluginArtifact, request,
                                                                          dependencyFilter, e ) );
            throw e;
        }
    }

}
