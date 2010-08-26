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
import org.commonjava.emb.event.EMBEvent;
import org.sonatype.aether.Artifact;
import org.sonatype.aether.DependencyFilter;
import org.sonatype.aether.DependencyNode;
import org.sonatype.aether.RemoteRepository;
import org.sonatype.aether.RepositorySystemSession;

import java.util.List;

public class PluginResolutionEvent
    extends EMBEvent
    implements ResolutionEvent
{

    private final Plugin plugin;

    private final DependencyFilter dependencyFilter;

    private final ResolutionEventType type;

    private final PluginResolutionException error;

    private final Artifact pluginArtifact;

    private final List<RemoteRepository> repositories;

    private final RepositorySystemSession session;

    private final DependencyNode resolvedNode;

    public PluginResolutionEvent( final Plugin plugin, final List<RemoteRepository> repositories,
                                  final RepositorySystemSession session )
    {
        this.plugin = plugin;
        this.repositories = repositories;
        this.session = session;
        type = ResolutionEventType.START;
        dependencyFilter = null;
        resolvedNode = null;
        error = null;
        pluginArtifact = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final List<RemoteRepository> repositories,
                                  final RepositorySystemSession session, final Artifact pluginArtifact )
    {
        this.plugin = plugin;
        this.repositories = repositories;
        this.session = session;
        this.pluginArtifact = pluginArtifact;
        type = ResolutionEventType.SUCCESS;
        dependencyFilter = null;
        error = null;
        resolvedNode = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final List<RemoteRepository> repositories,
                                  final RepositorySystemSession session, final PluginResolutionException error )
    {
        this.plugin = plugin;
        this.repositories = repositories;
        this.session = session;
        this.error = error;
        type = ResolutionEventType.FAIL;
        resolvedNode = null;
        dependencyFilter = null;
        pluginArtifact = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final Artifact pluginArtifact,
                                  final List<RemoteRepository> repositories, final RepositorySystemSession session,
                                  final DependencyFilter dependencyFilter )
    {
        this.plugin = plugin;
        this.pluginArtifact = pluginArtifact;
        this.repositories = repositories;
        this.session = session;
        type = ResolutionEventType.START;
        this.dependencyFilter = dependencyFilter;
        resolvedNode = null;
        error = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final Artifact pluginArtifact,
                                  final List<RemoteRepository> repositories, final RepositorySystemSession session,
                                  final DependencyFilter dependencyFilter, final DependencyNode result )
    {
        this.plugin = plugin;
        this.pluginArtifact = pluginArtifact;
        this.repositories = repositories;
        this.session = session;
        resolvedNode = result;
        type = ResolutionEventType.SUCCESS;
        this.dependencyFilter = dependencyFilter;
        error = null;
    }

    public PluginResolutionEvent( final Plugin plugin, final Artifact pluginArtifact,
                                  final List<RemoteRepository> repositories, final RepositorySystemSession session,
                                  final DependencyFilter dependencyFilter, final PluginResolutionException error )
    {
        this.plugin = plugin;
        this.pluginArtifact = pluginArtifact;
        this.repositories = repositories;
        this.session = session;
        this.error = error;
        type = ResolutionEventType.FAIL;
        this.dependencyFilter = dependencyFilter;
        resolvedNode = null;
    }

    public ResolutionEventType getType()
    {
        return type;
    }

    public Plugin getPlugin()
    {
        return plugin;
    }

    public DependencyFilter getDependencyFilter()
    {
        return dependencyFilter;
    }

    public DependencyNode getResolvedNode()
    {
        return resolvedNode;
    }

    public PluginResolutionException getError()
    {
        return error;
    }

    public Artifact getPluginArtifact()
    {
        return pluginArtifact;
    }

    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }

    public RepositorySystemSession getSession()
    {
        return session;
    }

}
