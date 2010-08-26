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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.PluginResolutionException;
import org.commonjava.emb.event.EMBEvent;

import java.util.Collections;
import java.util.List;

public class PluginResolutionEvent
    extends EMBEvent
    implements ResolutionEvent
{

    private final Plugin plugin;

    private final ArtifactFilter dependencyFilter;

    private final ArtifactResolutionRequest request;

    private final List<Artifact> resolvedArtifacts;

    private final ResolutionEventType type;

    private final PluginResolutionException error;

    private final Artifact pluginArtifact;

    public PluginResolutionEvent( final Plugin plugin, final ArtifactResolutionRequest request )
    {
        this.plugin = plugin;
        this.request = request;
        type = ResolutionEventType.START;
        dependencyFilter = null;
        resolvedArtifacts = null;
        error = null;
        pluginArtifact = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final ArtifactResolutionRequest request,
                                  final Artifact pluginArtifact )
    {
        this.plugin = plugin;
        this.request = request;
        type = ResolutionEventType.SUCCESS;
        resolvedArtifacts = Collections.singletonList( pluginArtifact );
        dependencyFilter = null;
        error = null;
        this.pluginArtifact = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final ArtifactResolutionRequest request,
                                  final PluginResolutionException error )
    {
        this.plugin = plugin;
        this.request = request;
        this.error = error;
        type = ResolutionEventType.FAIL;
        resolvedArtifacts = null;
        dependencyFilter = null;
        pluginArtifact = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final Artifact pluginArtifact,
                                  final ArtifactResolutionRequest request, final ArtifactFilter dependencyFilter )
    {
        this.plugin = plugin;
        this.pluginArtifact = pluginArtifact;
        this.request = request;
        type = ResolutionEventType.START;
        this.dependencyFilter = dependencyFilter;
        resolvedArtifacts = null;
        error = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final Artifact pluginArtifact,
                                  final ArtifactResolutionRequest request, final ArtifactFilter dependencyFilter,
                                  final List<Artifact> pluginArtifacts )
    {
        this.plugin = plugin;
        this.pluginArtifact = pluginArtifact;
        this.request = request;
        type = ResolutionEventType.SUCCESS;
        this.dependencyFilter = dependencyFilter;
        resolvedArtifacts = pluginArtifacts;
        error = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final Artifact pluginArtifact,
                                  final ArtifactResolutionRequest request, final ArtifactFilter dependencyFilter,
                                  final PluginResolutionException error )
    {
        this.plugin = plugin;
        this.pluginArtifact = pluginArtifact;
        this.request = request;
        this.error = error;
        type = ResolutionEventType.FAIL;
        this.dependencyFilter = dependencyFilter;
        resolvedArtifacts = null;
    }

    public ArtifactResolutionRequest getRequest()
    {
        return request;
    }

    public ResolutionEventType getType()
    {
        return type;
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public ArtifactFilter getDependencyFilter()
    {
        return dependencyFilter;
    }

    public List<Artifact> getResolvedArtifacts()
    {
        return resolvedArtifacts;
    }

    public PluginResolutionException getError()
    {
        return error;
    }

    public Artifact getPluginArtifact()
    {
        return pluginArtifact;
    }

}
