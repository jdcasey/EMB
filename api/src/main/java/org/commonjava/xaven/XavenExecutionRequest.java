package org.commonjava.xaven;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryCache;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Profile;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.ArtifactTransferListener;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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

public interface XavenExecutionRequest
{

    XavenExecutionRequest copyOf();

    MavenExecutionRequest asMavenExecutionRequest();

    XavenExecutionRequest setPasswordToEncrypt( final String password );

    String getPasswordToEncyrpt();

    XavenExecutionRequest setSettings( final Settings settings );

    Settings getSettings();

    // NOTE: Methods below are adapted from MavenExecutionRequest

    // Base directory
    XavenExecutionRequest setBaseDirectory( File basedir );

    String getBaseDirectory();

    // Timing (remove this)
    XavenExecutionRequest setStartTime( Date start );

    Date getStartTime();

    // Goals
    XavenExecutionRequest withPluginGoals( PluginGoal... goal );

    XavenExecutionRequest withPluginGoal( PluginGoal goal );

    XavenExecutionRequest setGoals( List<String> goals );

    List<String> getGoals();

    // Properties

    /**
     * Sets the system properties to use for interpolation and profile activation. The system properties are collected
     * from the runtime environment like {@link System#getProperties()} and environment variables.
     * 
     * @param systemProperties The system properties, may be {@code null}.
     * @return This request, never {@code null}.
     */
    XavenExecutionRequest setSystemProperties( Properties systemProperties );

    XavenExecutionRequest setSystemProperty( String key, String value );

    /**
     * Gets the system properties to use for interpolation and profile activation. The system properties are collected
     * from the runtime environment like {@link System#getProperties()} and environment variables.
     * 
     * @return The system properties, never {@code null}.
     */
    Properties getSystemProperties();

    /**
     * Sets the user properties to use for interpolation and profile activation. The user properties have been
     * configured directly by the user on his discretion, e.g. via the {@code -Dkey=value} parameter on the command
     * line.
     * 
     * @param userProperties The user properties, may be {@code null}.
     * @return This request, never {@code null}.
     */
    XavenExecutionRequest setUserProperties( Properties userProperties );

    XavenExecutionRequest setUserProperty( String key, String value );

    /**
     * Gets the user properties to use for interpolation and profile activation. The user properties have been
     * configured directly by the user on his discretion, e.g. via the {@code -Dkey=value} parameter on the command
     * line.
     * 
     * @return The user properties, never {@code null}.
     */
    Properties getUserProperties();

    // Reactor
    XavenExecutionRequest setReactorFailureBehavior( String failureBehavior );

    String getReactorFailureBehavior();

    XavenExecutionRequest setSelectedProjects( List<String> projects );

    List<String> getSelectedProjects();

    XavenExecutionRequest setResumeFrom( String project );

    String getResumeFrom();

    XavenExecutionRequest setMakeBehavior( String makeBehavior );

    String getMakeBehavior();

    XavenExecutionRequest setThreadCount( String threadCount );

    String getThreadCount();

    boolean isThreadConfigurationPresent();

    XavenExecutionRequest setPerCoreThreadCount( boolean perCoreThreadCount );

    boolean isPerCoreThreadCount();

    // Recursive (really to just process the top-level POM)
    XavenExecutionRequest setRecursive( boolean recursive );

    boolean isRecursive();

    XavenExecutionRequest setPom( File pom );

    File getPom();

    // Errors
    XavenExecutionRequest setShowErrors( boolean showErrors );

    boolean isShowErrors();

    // Transfer listeners
    XavenExecutionRequest setTransferListener( ArtifactTransferListener transferListener );

    ArtifactTransferListener getTransferListener();

    // Logging
    XavenExecutionRequest setLoggingLevel( int loggingLevel );

    int getLoggingLevel();

    // Update snapshots
    XavenExecutionRequest setUpdateSnapshots( boolean updateSnapshots );

    boolean isUpdateSnapshots();

    XavenExecutionRequest setNoSnapshotUpdates( boolean noSnapshotUpdates );

    boolean isNoSnapshotUpdates();

    // Checksum policy
    XavenExecutionRequest setGlobalChecksumPolicy( String globalChecksumPolicy );

    String getGlobalChecksumPolicy();

    // Local repository
    XavenExecutionRequest setLocalRepositoryPath( String localRepository );

    XavenExecutionRequest setLocalRepositoryPath( File localRepository );

    File getLocalRepositoryPath();

    XavenExecutionRequest setLocalRepository( ArtifactRepository repository );

    ArtifactRepository getLocalRepository();

    // Interactive
    XavenExecutionRequest setInteractiveMode( boolean interactive );

    boolean isInteractiveMode();

    // Offline
    XavenExecutionRequest setOffline( boolean offline );

    boolean isOffline();

    // Profiles
    List<Profile> getProfiles();

    XavenExecutionRequest addProfile( Profile profile );

    XavenExecutionRequest setProfiles( List<Profile> profiles );

    XavenExecutionRequest addActiveProfile( String profile );

    XavenExecutionRequest addActiveProfiles( List<String> profiles );

    XavenExecutionRequest setActiveProfiles( List<String> profiles );

    List<String> getActiveProfiles();

    XavenExecutionRequest addInactiveProfile( String profile );

    XavenExecutionRequest addInactiveProfiles( List<String> profiles );

    XavenExecutionRequest setInactiveProfiles( List<String> profiles );

    List<String> getInactiveProfiles();

    // Proxies
    List<Proxy> getProxies();

    XavenExecutionRequest setProxies( List<Proxy> proxies );

    XavenExecutionRequest addProxy( Proxy proxy );

    // Servers
    List<Server> getServers();

    XavenExecutionRequest setServers( List<Server> servers );

    XavenExecutionRequest addServer( Server server );

    // Mirrors
    List<Mirror> getMirrors();

    XavenExecutionRequest setMirrors( List<Mirror> mirrors );

    XavenExecutionRequest addMirror( Mirror mirror );

    // Plugin groups
    List<String> getPluginGroups();

    XavenExecutionRequest setPluginGroups( List<String> pluginGroups );

    XavenExecutionRequest addPluginGroup( String pluginGroup );

    XavenExecutionRequest addPluginGroups( List<String> pluginGroups );

    boolean isProjectPresent();

    XavenExecutionRequest setProjectPresent( boolean isProjectPresent );

    File getUserSettingsFile();

    XavenExecutionRequest setUserSettingsFile( File userSettingsFile );

    File getGlobalSettingsFile();

    XavenExecutionRequest setGlobalSettingsFile( File globalSettingsFile );

    XavenExecutionRequest addRemoteRepository( ArtifactRepository repository );

    XavenExecutionRequest addPluginArtifactRepository( ArtifactRepository repository );

    /**
     * Set a new list of remote repositories to use the execution request. This is necessary if you perform
     * transformations on the remote repositories being used. For example if you replace existing repositories with
     * mirrors then it's easier to just replace the whole list with a new list of transformed repositories.
     * 
     * @param repositories
     * @return
     */
    XavenExecutionRequest setRemoteRepositories( List<ArtifactRepository> repositories );

    List<ArtifactRepository> getRemoteRepositories();

    XavenExecutionRequest setPluginArtifactRepositories( List<ArtifactRepository> repositories );

    List<ArtifactRepository> getPluginArtifactRepositories();

    XavenExecutionRequest setRepositoryCache( RepositoryCache repositoryCache );

    RepositoryCache getRepositoryCache();

    File getUserToolchainsFile();

    XavenExecutionRequest setUserToolchainsFile( File userToolchainsFile );

    ExecutionListener getExecutionListener();

    XavenExecutionRequest setExecutionListener( ExecutionListener executionListener );

    ProjectBuildingRequest getProjectBuildingRequest();

}