package org.commonjava.emb;

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

public interface EMBExecutionRequest
{

    EMBExecutionRequest copyOf();

    MavenExecutionRequest asMavenExecutionRequest();

    EMBExecutionRequest setPasswordToEncrypt( final String password );

    String getPasswordToEncyrpt();

    EMBExecutionRequest setSettings( final Settings settings );

    Settings getSettings();

    // NOTE: Methods below are adapted from MavenExecutionRequest

    // Base directory
    EMBExecutionRequest setBaseDirectory( File basedir );

    String getBaseDirectory();

    // Timing (remove this)
    EMBExecutionRequest setStartTime( Date start );

    Date getStartTime();

    // Goals
    EMBExecutionRequest withPluginGoals( PluginGoal... goal );

    EMBExecutionRequest withPluginGoal( PluginGoal goal );

    EMBExecutionRequest setGoals( List<String> goals );

    List<String> getGoals();

    // Properties

    /**
     * Sets the system properties to use for interpolation and profile activation. The system properties are collected
     * from the runtime environment like {@link System#getProperties()} and environment variables.
     * 
     * @param systemProperties The system properties, may be {@code null}.
     * @return This request, never {@code null}.
     */
    EMBExecutionRequest setSystemProperties( Properties systemProperties );

    EMBExecutionRequest setSystemProperty( String key, String value );

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
    EMBExecutionRequest setUserProperties( Properties userProperties );

    EMBExecutionRequest setUserProperty( String key, String value );

    /**
     * Gets the user properties to use for interpolation and profile activation. The user properties have been
     * configured directly by the user on his discretion, e.g. via the {@code -Dkey=value} parameter on the command
     * line.
     * 
     * @return The user properties, never {@code null}.
     */
    Properties getUserProperties();

    // Reactor
    EMBExecutionRequest setReactorFailureBehavior( String failureBehavior );

    String getReactorFailureBehavior();

    EMBExecutionRequest setSelectedProjects( List<String> projects );

    List<String> getSelectedProjects();

    EMBExecutionRequest setResumeFrom( String project );

    String getResumeFrom();

    EMBExecutionRequest setMakeBehavior( String makeBehavior );

    String getMakeBehavior();

    EMBExecutionRequest setThreadCount( String threadCount );

    String getThreadCount();

    boolean isThreadConfigurationPresent();

    EMBExecutionRequest setPerCoreThreadCount( boolean perCoreThreadCount );

    boolean isPerCoreThreadCount();

    // Recursive (really to just process the top-level POM)
    EMBExecutionRequest setRecursive( boolean recursive );

    boolean isRecursive();

    EMBExecutionRequest setPom( File pom );

    File getPom();

    // Errors
    EMBExecutionRequest setShowErrors( boolean showErrors );

    boolean isShowErrors();

    // Transfer listeners
    EMBExecutionRequest setTransferListener( ArtifactTransferListener transferListener );

    ArtifactTransferListener getTransferListener();

    // Logging
    EMBExecutionRequest setLoggingLevel( int loggingLevel );

    int getLoggingLevel();

    // Update snapshots
    EMBExecutionRequest setUpdateSnapshots( boolean updateSnapshots );

    boolean isUpdateSnapshots();

    EMBExecutionRequest setNoSnapshotUpdates( boolean noSnapshotUpdates );

    boolean isNoSnapshotUpdates();

    // Checksum policy
    EMBExecutionRequest setGlobalChecksumPolicy( String globalChecksumPolicy );

    String getGlobalChecksumPolicy();

    // Local repository
    EMBExecutionRequest setLocalRepositoryPath( String localRepository );

    EMBExecutionRequest setLocalRepositoryPath( File localRepository );

    File getLocalRepositoryPath();

    EMBExecutionRequest setLocalRepository( ArtifactRepository repository );

    ArtifactRepository getLocalRepository();

    // Interactive
    EMBExecutionRequest setInteractiveMode( boolean interactive );

    boolean isInteractiveMode();

    // Offline
    EMBExecutionRequest setOffline( boolean offline );

    boolean isOffline();

    // Profiles
    List<Profile> getProfiles();

    EMBExecutionRequest addProfile( Profile profile );

    EMBExecutionRequest setProfiles( List<Profile> profiles );

    EMBExecutionRequest addActiveProfile( String profile );

    EMBExecutionRequest addActiveProfiles( List<String> profiles );

    EMBExecutionRequest setActiveProfiles( List<String> profiles );

    List<String> getActiveProfiles();

    EMBExecutionRequest addInactiveProfile( String profile );

    EMBExecutionRequest addInactiveProfiles( List<String> profiles );

    EMBExecutionRequest setInactiveProfiles( List<String> profiles );

    List<String> getInactiveProfiles();

    // Proxies
    List<Proxy> getProxies();

    EMBExecutionRequest setProxies( List<Proxy> proxies );

    EMBExecutionRequest addProxy( Proxy proxy );

    // Servers
    List<Server> getServers();

    EMBExecutionRequest setServers( List<Server> servers );

    EMBExecutionRequest addServer( Server server );

    // Mirrors
    List<Mirror> getMirrors();

    EMBExecutionRequest setMirrors( List<Mirror> mirrors );

    EMBExecutionRequest addMirror( Mirror mirror );

    // Plugin groups
    List<String> getPluginGroups();

    EMBExecutionRequest setPluginGroups( List<String> pluginGroups );

    EMBExecutionRequest addPluginGroup( String pluginGroup );

    EMBExecutionRequest addPluginGroups( List<String> pluginGroups );

    boolean isProjectPresent();

    EMBExecutionRequest setProjectPresent( boolean isProjectPresent );

    File getUserSettingsFile();

    EMBExecutionRequest setUserSettingsFile( File userSettingsFile );

    File getGlobalSettingsFile();

    EMBExecutionRequest setGlobalSettingsFile( File globalSettingsFile );

    EMBExecutionRequest addRemoteRepository( ArtifactRepository repository );

    EMBExecutionRequest addPluginArtifactRepository( ArtifactRepository repository );

    /**
     * Set a new list of remote repositories to use the execution request. This is necessary if you perform
     * transformations on the remote repositories being used. For example if you replace existing repositories with
     * mirrors then it's easier to just replace the whole list with a new list of transformed repositories.
     * 
     * @param repositories
     * @return
     */
    EMBExecutionRequest setRemoteRepositories( List<ArtifactRepository> repositories );

    List<ArtifactRepository> getRemoteRepositories();

    EMBExecutionRequest setPluginArtifactRepositories( List<ArtifactRepository> repositories );

    List<ArtifactRepository> getPluginArtifactRepositories();

    EMBExecutionRequest setRepositoryCache( RepositoryCache repositoryCache );

    RepositoryCache getRepositoryCache();

    File getUserToolchainsFile();

    EMBExecutionRequest setUserToolchainsFile( File userToolchainsFile );

    ExecutionListener getExecutionListener();

    EMBExecutionRequest setExecutionListener( ExecutionListener executionListener );

    ProjectBuildingRequest getProjectBuildingRequest();

}
