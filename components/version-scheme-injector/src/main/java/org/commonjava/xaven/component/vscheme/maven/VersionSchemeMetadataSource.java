package org.commonjava.xaven.component.vscheme.maven;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Relocation;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.artifact.ArtifactWithDependencies;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.apache.maven.project.artifact.MavenMetadataCache;
import org.apache.maven.properties.internal.EnvironmentUtils;
import org.apache.maven.repository.legacy.metadata.DefaultMetadataResolutionRequest;
import org.apache.maven.repository.legacy.metadata.MetadataResolutionRequest;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Jason van Zyl
 */
@Component( role = ArtifactMetadataSource.class, hint = "vscheme" )
public class VersionSchemeMetadataSource
    implements ArtifactMetadataSource
{
    @Requirement
    private RepositoryMetadataManager repositoryMetadataManager;

    @Requirement
    private ArtifactFactory repositorySystem;

    //TODO: This prevents a cycle in the composition which shows us another problem we need to deal with.
    //@Requirement
    private ProjectBuilder projectBuilder;

    @Requirement
    private PlexusContainer container;

    @Requirement
    private Logger logger;

    @Requirement
    private MavenMetadataCache cache;

    @Requirement
    private LegacySupport legacySupport;

    private void injectSession( final MetadataResolutionRequest request )
    {
        final MavenSession session = legacySupport.getSession();

        if ( session != null )
        {
            request.setOffline( session.isOffline() );
            request.setServers( session.getRequest().getServers() );
            request.setMirrors( session.getRequest().getMirrors() );
            request.setProxies( session.getRequest().getProxies() );
            request.setTransferListener( session.getRequest().getTransferListener() );
        }
    }

    public ResolutionGroup retrieve( final Artifact artifact, final ArtifactRepository localRepository,
                                     final List<ArtifactRepository> remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        return retrieve( artifact, localRepository, remoteRepositories, false );
    }

    public ResolutionGroup retrieve( final Artifact artifact, final ArtifactRepository localRepository,
                                     final List<ArtifactRepository> remoteRepositories,
                                     final boolean resolveManagedVersions )
        throws ArtifactMetadataRetrievalException
    {
        final MetadataResolutionRequest request = new DefaultMetadataResolutionRequest();
        injectSession( request );
        request.setArtifact( artifact );
        request.setLocalRepository( localRepository );
        request.setRemoteRepositories( remoteRepositories );
        request.setResolveManagedVersions( resolveManagedVersions );
        return retrieve( request );
    }

    public ResolutionGroup retrieve( final MetadataResolutionRequest request )
        throws ArtifactMetadataRetrievalException
    {
        final Artifact artifact = request.getArtifact();

        //
        // If we have a system scoped artifact then we do not want any searching in local or remote repositories
        // and we want artifact resolution to only return the system scoped artifact itself.
        //
        if ( artifact.getScope() != null && artifact.getScope().equals( Artifact.SCOPE_SYSTEM ) )
        {
            return new ResolutionGroup( null, null, null );
        }

        final ResolutionGroup cached =
            cache.get( artifact, request.isResolveManagedVersions(), request.getLocalRepository(),
                       request.getRemoteRepositories() );

        if ( cached != null )
        {
            // if the POM has no file, we cached a missing artifact, only return the cached data if no update forced
            if ( !request.isForceUpdate() || hasFile( cached.getPomArtifact() ) )
            {
                return cached;
            }
        }

        List<Dependency> dependencies;

        List<Dependency> managedDependencies = null;

        List<ArtifactRepository> pomRepositories = null;

        Artifact pomArtifact;

        Artifact relocatedArtifact = null;

        //TODO: Not even sure this is really required as the project will be cached in the builder, we'll see this
        // is currently the biggest hotspot
        if ( artifact instanceof ArtifactWithDependencies )
        {
            pomArtifact = artifact;

            dependencies = ( (ArtifactWithDependencies) artifact ).getDependencies();

            managedDependencies = ( (ArtifactWithDependencies) artifact ).getManagedDependencies();
        }
        else
        {
            final ProjectRelocation rel = retrieveRelocatedProject( artifact, request );

            if ( rel == null )
            {
                return null;
            }

            pomArtifact = rel.pomArtifact;

            relocatedArtifact = rel.relocatedArtifact;

            if ( rel.project == null )
            {
                // When this happens we have a Maven 1.x POM, or some invalid POM. There is still a pile of
                // shit in the Maven 2.x repository that should have never found its way into the repository
                // but it did.
                dependencies = Collections.emptyList();
            }
            else
            {
                dependencies = rel.project.getDependencies();

                final DependencyManagement depMngt = rel.project.getDependencyManagement();
                managedDependencies = ( depMngt != null ) ? depMngt.getDependencies() : null;

                pomRepositories = rel.project.getRemoteArtifactRepositories();
            }
        }

        Set<Artifact> artifacts = Collections.<Artifact> emptySet();

        if ( !artifact.getArtifactHandler().isIncludesDependencies() )
        {
            artifacts = new LinkedHashSet<Artifact>();

            for ( final Dependency dependency : dependencies )
            {
                final Artifact dependencyArtifact = createDependencyArtifact( dependency, artifact, pomArtifact );

                if ( dependencyArtifact != null )
                {
                    artifacts.add( dependencyArtifact );
                }
            }
        }

        Map<String, Artifact> managedVersions = null;

        if ( managedDependencies != null && request.isResolveManagedVersions() )
        {
            managedVersions = new HashMap<String, Artifact>();

            for ( final Dependency managedDependency : managedDependencies )
            {
                final Artifact managedArtifact = createDependencyArtifact( managedDependency, null, pomArtifact );

                managedVersions.put( managedDependency.getManagementKey(), managedArtifact );
            }
        }

        final List<ArtifactRepository> aggregatedRepositories =
            aggregateRepositories( request.getRemoteRepositories(), pomRepositories );

        final ResolutionGroup result =
            new ResolutionGroup( pomArtifact, relocatedArtifact, artifacts, managedVersions, aggregatedRepositories );

        cache.put( artifact, request.isResolveManagedVersions(), request.getLocalRepository(),
                   request.getRemoteRepositories(), result );

        return result;
    }

    private boolean hasFile( final Artifact artifact )
    {
        return artifact != null && artifact.getFile() != null && artifact.getFile().exists();
    }

    private List<ArtifactRepository> aggregateRepositories( final List<ArtifactRepository> requestRepositories,
                                                            final List<ArtifactRepository> pomRepositories )
    {
        List<ArtifactRepository> repositories = requestRepositories;

        if ( pomRepositories != null && !pomRepositories.isEmpty() )
        {
            final Map<String, ArtifactRepository> repos = new LinkedHashMap<String, ArtifactRepository>();

            for ( final ArtifactRepository repo : requestRepositories )
            {
                if ( !repos.containsKey( repo.getId() ) )
                {
                    repos.put( repo.getId(), repo );
                }
            }

            for ( final ArtifactRepository repo : pomRepositories )
            {
                if ( !repos.containsKey( repo.getId() ) )
                {
                    repos.put( repo.getId(), repo );
                }
            }

            repositories = new ArrayList<ArtifactRepository>( repos.values() );
        }

        return repositories;
    }

    private Artifact createDependencyArtifact( final Dependency dependency, final Artifact owner, final Artifact pom )
        throws ArtifactMetadataRetrievalException
    {
        try
        {
            final String inheritedScope = ( owner != null ) ? owner.getScope() : null;

            final ArtifactFilter inheritedFilter = ( owner != null ) ? owner.getDependencyFilter() : null;

            return createDependencyArtifact( repositorySystem, dependency, inheritedScope, inheritedFilter );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            throw new ArtifactMetadataRetrievalException( "Invalid version for dependency "
                + dependency.getManagementKey() + ": " + e.getMessage(), e, pom );
        }
    }

    private static Artifact createDependencyArtifact( final ArtifactFactory factory, final Dependency dependency,
                                                      final String inheritedScope, final ArtifactFilter inheritedFilter )
        throws InvalidVersionSpecificationException
    {
        final String effectiveScope = getEffectiveScope( dependency.getScope(), inheritedScope );

        if ( effectiveScope == null )
        {
            return null;
        }

        final VersionRange versionRange = VersionRange.createFromVersionSpec( dependency.getVersion() );

        final Artifact dependencyArtifact =
            factory.createDependencyArtifact( dependency.getGroupId(), dependency.getArtifactId(), versionRange,
                                              dependency.getType(), dependency.getClassifier(), effectiveScope,
                                              dependency.isOptional() );

        final ArtifactFilter dependencyFilter = inheritedFilter;

        if ( dependencyFilter != null && !dependencyFilter.include( dependencyArtifact ) )
        {
            return null;
        }

        if ( Artifact.SCOPE_SYSTEM.equals( effectiveScope ) )
        {
            dependencyArtifact.setFile( new File( dependency.getSystemPath() ) );
        }

        dependencyArtifact.setDependencyFilter( createDependencyFilter( dependency, dependencyFilter ) );

        return dependencyArtifact;
    }

    private static String getEffectiveScope( String originalScope, final String inheritedScope )
    {
        String effectiveScope = Artifact.SCOPE_RUNTIME;

        if ( originalScope == null )
        {
            originalScope = Artifact.SCOPE_COMPILE;
        }

        if ( inheritedScope == null )
        {
            // direct dependency retains its scope
            effectiveScope = originalScope;
        }
        else if ( Artifact.SCOPE_TEST.equals( originalScope ) || Artifact.SCOPE_PROVIDED.equals( originalScope ) )
        {
            // test and provided are not transitive, so exclude them
            effectiveScope = null;
        }
        else if ( Artifact.SCOPE_SYSTEM.equals( originalScope ) )
        {
            // system scope come through unchanged...
            effectiveScope = Artifact.SCOPE_SYSTEM;
        }
        else if ( Artifact.SCOPE_COMPILE.equals( originalScope ) && Artifact.SCOPE_COMPILE.equals( inheritedScope ) )
        {
            // added to retain compile scope. Remove if you want compile inherited as runtime
            effectiveScope = Artifact.SCOPE_COMPILE;
        }
        else if ( Artifact.SCOPE_TEST.equals( inheritedScope ) )
        {
            effectiveScope = Artifact.SCOPE_TEST;
        }
        else if ( Artifact.SCOPE_PROVIDED.equals( inheritedScope ) )
        {
            effectiveScope = Artifact.SCOPE_PROVIDED;
        }

        return effectiveScope;
    }

    private static ArtifactFilter createDependencyFilter( final Dependency dependency,
                                                          final ArtifactFilter inheritedFilter )
    {
        ArtifactFilter effectiveFilter = inheritedFilter;

        if ( !dependency.getExclusions().isEmpty() )
        {
            final List<String> exclusions = new ArrayList<String>();

            for ( final Exclusion e : dependency.getExclusions() )
            {
                exclusions.add( e.getGroupId() + ':' + e.getArtifactId() );
            }

            effectiveFilter = new ExcludesArtifactFilter( exclusions );

            if ( inheritedFilter != null )
            {
                effectiveFilter = new AndArtifactFilter( Arrays.asList( inheritedFilter, effectiveFilter ) );
            }
        }

        return effectiveFilter;
    }

    public List<ArtifactVersion> retrieveAvailableVersions( final Artifact artifact,
                                                            final ArtifactRepository localRepository,
                                                            final List<ArtifactRepository> remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        final MetadataResolutionRequest request = new DefaultMetadataResolutionRequest();
        injectSession( request );
        request.setArtifact( artifact );
        request.setLocalRepository( localRepository );
        request.setRemoteRepositories( remoteRepositories );
        return retrieveAvailableVersions( request );
    }

    public List<ArtifactVersion> retrieveAvailableVersions( final MetadataResolutionRequest request )
        throws ArtifactMetadataRetrievalException
    {
        final RepositoryMetadata metadata = new ArtifactRepositoryMetadata( request.getArtifact() );

        try
        {
            repositoryMetadataManager.resolve( metadata, request );
        }
        catch ( final RepositoryMetadataResolutionException e )
        {
            throw new ArtifactMetadataRetrievalException( e.getMessage(), e, request.getArtifact() );
        }

        final List<String> availableVersions = request.getLocalRepository().findVersions( request.getArtifact() );

        return retrieveAvailableVersionsFromMetadata( metadata.getMetadata(), availableVersions );
    }

    public List<ArtifactVersion> retrieveAvailableVersionsFromDeploymentRepository( final Artifact artifact,
                                                                                    final ArtifactRepository localRepository,
                                                                                    final ArtifactRepository deploymentRepository )
        throws ArtifactMetadataRetrievalException
    {
        final RepositoryMetadata metadata = new ArtifactRepositoryMetadata( artifact );

        try
        {
            repositoryMetadataManager.resolveAlways( metadata, localRepository, deploymentRepository );
        }
        catch ( final RepositoryMetadataResolutionException e )
        {
            throw new ArtifactMetadataRetrievalException( e.getMessage(), e, artifact );
        }

        final List<String> availableVersions = localRepository.findVersions( artifact );

        return retrieveAvailableVersionsFromMetadata( metadata.getMetadata(), availableVersions );
    }

    private List<ArtifactVersion> retrieveAvailableVersionsFromMetadata( final Metadata repoMetadata,
                                                                         final List<String> availableVersions )
    {
        final Collection<String> versions = new LinkedHashSet<String>();

        if ( ( repoMetadata != null ) && ( repoMetadata.getVersioning() != null ) )
        {
            versions.addAll( repoMetadata.getVersioning().getVersions() );
        }

        versions.addAll( availableVersions );

        final List<ArtifactVersion> artifactVersions = new ArrayList<ArtifactVersion>( versions.size() );

        for ( final String version : versions )
        {
            artifactVersions.add( new DefaultArtifactVersion( version ) );
        }

        return artifactVersions;
    }

    // USED BY MAVEN ASSEMBLY PLUGIN
    @Deprecated
    public static Set<Artifact> createArtifacts( final ArtifactFactory artifactFactory,
                                                 final List<Dependency> dependencies, final String inheritedScope,
                                                 final ArtifactFilter dependencyFilter, final MavenProject project )
        throws InvalidDependencyVersionException
    {
        final Set<Artifact> artifacts = new LinkedHashSet<Artifact>();

        for ( final Dependency d : dependencies )
        {
            Artifact dependencyArtifact;
            try
            {
                dependencyArtifact = createDependencyArtifact( artifactFactory, d, inheritedScope, dependencyFilter );
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                throw new InvalidDependencyVersionException( project.getId(), d, project.getFile(), e );
            }

            if ( dependencyArtifact != null )
            {
                artifacts.add( dependencyArtifact );
            }
        }

        return artifacts;
    }

    private ProjectBuilder getProjectBuilder()
    {
        if ( projectBuilder != null )
        {
            return projectBuilder;
        }

        try
        {
            projectBuilder = container.lookup( ProjectBuilder.class );
        }
        catch ( final ComponentLookupException e )
        {
            // Won't happen
        }

        return projectBuilder;
    }

    private ProjectRelocation retrieveRelocatedProject( final Artifact artifact,
                                                        final MetadataResolutionRequest repositoryRequest )
        throws ArtifactMetadataRetrievalException
    {
        MavenProject project;

        Artifact pomArtifact;
        Artifact relocatedArtifact = null;
        boolean done = false;
        do
        {
            project = null;

            pomArtifact =
                repositorySystem.createProjectArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                                                        artifact.getVersion(), artifact.getScope() );

            if ( "pom".equals( artifact.getType() ) )
            {
                pomArtifact.setFile( artifact.getFile() );
            }

            if ( Artifact.SCOPE_SYSTEM.equals( artifact.getScope() ) )
            {
                done = true;
            }
            else
            {
                try
                {
                    final ProjectBuildingRequest configuration = new DefaultProjectBuildingRequest();
                    configuration.setRepositoryCache( repositoryRequest.getCache() );
                    configuration.setLocalRepository( repositoryRequest.getLocalRepository() );
                    configuration.setRemoteRepositories( repositoryRequest.getRemoteRepositories() );
                    configuration.setOffline( repositoryRequest.isOffline() );
                    configuration.setForceUpdate( repositoryRequest.isForceUpdate() );
                    configuration.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
                    configuration.setProcessPlugins( false );
                    configuration.setSystemProperties( getSystemProperties() );
                    configuration.setTransferListener( repositoryRequest.getTransferListener() );
                    configuration.setServers( repositoryRequest.getServers() );
                    configuration.setMirrors( repositoryRequest.getMirrors() );
                    configuration.setProxies( repositoryRequest.getProxies() );

                    project = getProjectBuilder().build( pomArtifact, configuration ).getProject();
                }
                catch ( final ProjectBuildingException e )
                {
                    final ModelProblem missingParentPom = hasMissingParentPom( e );
                    if ( missingParentPom != null )
                    {
                        throw new ArtifactMetadataRetrievalException( "Failed to process POM for " + artifact.getId()
                            + ": " + missingParentPom.getMessage(), missingParentPom.getException(), artifact );
                    }

                    String message;

                    if ( e.getCause() instanceof MultipleArtifactsNotFoundException )
                    {
                        message = "Missing POM for " + artifact.getId();
                    }
                    else if ( e.getCause() instanceof ArtifactResolutionException )
                    {
                        throw new ArtifactMetadataRetrievalException( "Failed to retrieve POM for " + artifact.getId()
                            + ": " + e.getCause().getMessage(), e.getCause(), artifact );
                    }
                    else
                    {
                        message =
                            "Invalid POM for " + artifact.getId()
                                + ", transitive dependencies (if any) will not be available"
                                + ", enable debug logging for more details";
                    }

                    if ( logger.isDebugEnabled() )
                    {
                        message += ": " + e.getMessage();
                    }

                    logger.warn( message );
                }

                if ( project != null )
                {
                    Relocation relocation = null;

                    final DistributionManagement distMgmt = project.getDistributionManagement();
                    if ( distMgmt != null )
                    {
                        relocation = distMgmt.getRelocation();

                        artifact.setDownloadUrl( distMgmt.getDownloadUrl() );
                        pomArtifact.setDownloadUrl( distMgmt.getDownloadUrl() );
                    }

                    if ( relocation != null )
                    {
                        if ( relocation.getGroupId() != null )
                        {
                            artifact.setGroupId( relocation.getGroupId() );
                            relocatedArtifact = artifact;
                            project.setGroupId( relocation.getGroupId() );
                        }
                        if ( relocation.getArtifactId() != null )
                        {
                            artifact.setArtifactId( relocation.getArtifactId() );
                            relocatedArtifact = artifact;
                            project.setArtifactId( relocation.getArtifactId() );
                        }
                        if ( relocation.getVersion() != null )
                        {
                            // note: see MNG-3454. This causes a problem, but fixing it may break more.
                            artifact.setVersionRange( VersionRange.createFromVersion( relocation.getVersion() ) );
                            relocatedArtifact = artifact;
                            project.setVersion( relocation.getVersion() );
                        }

                        if ( artifact.getDependencyFilter() != null
                            && !artifact.getDependencyFilter().include( artifact ) )
                        {
                            return null;
                        }

                        // MNG-2861: the artifact data has changed. If the available versions where previously
                        // retrieved, we need to update it.
                        // TODO: shouldn't the versions be merged across relocations?
                        List<ArtifactVersion> available = artifact.getAvailableVersions();
                        if ( available != null && !available.isEmpty() )
                        {
                            final MetadataResolutionRequest metadataRequest =
                                new DefaultMetadataResolutionRequest( repositoryRequest );
                            metadataRequest.setArtifact( artifact );
                            available = retrieveAvailableVersions( metadataRequest );
                            artifact.setAvailableVersions( available );
                        }

                        String message =
                            "\n  This artifact has been relocated to " + artifact.getGroupId() + ":"
                                + artifact.getArtifactId() + ":" + artifact.getVersion() + ".\n";

                        if ( relocation.getMessage() != null )
                        {
                            message += "  " + relocation.getMessage() + "\n";
                        }

                        if ( artifact.getDependencyTrail() != null && artifact.getDependencyTrail().size() == 1 )
                        {
                            logger.warn( "While downloading " + pomArtifact.getGroupId() + ":"
                                + pomArtifact.getArtifactId() + ":" + pomArtifact.getVersion() + message + "\n" );
                        }
                        else
                        {
                            logger.debug( "While downloading " + pomArtifact.getGroupId() + ":"
                                + pomArtifact.getArtifactId() + ":" + pomArtifact.getVersion() + message + "\n" );
                        }
                    }
                    else
                    {
                        done = true;
                    }
                }
                else
                {
                    done = true;
                }
            }
        }
        while ( !done );

        final ProjectRelocation rel = new ProjectRelocation();
        rel.project = project;
        rel.pomArtifact = pomArtifact;
        rel.relocatedArtifact = relocatedArtifact;

        return rel;
    }

    private ModelProblem hasMissingParentPom( final ProjectBuildingException e )
    {
        if ( e.getCause() instanceof ModelBuildingException )
        {
            final ModelBuildingException mbe = (ModelBuildingException) e.getCause();
            for ( final ModelProblem problem : mbe.getProblems() )
            {
                if ( problem.getException() instanceof UnresolvableModelException )
                {
                    return problem;
                }
            }

        }
        return null;
    }

    private Properties getSystemProperties()
    {
        final Properties props = new Properties();

        EnvironmentUtils.addEnvVars( props );

        props.putAll( System.getProperties() );

        return props;
    }

    private static final class ProjectRelocation
    {
        private MavenProject project;

        private Artifact pomArtifact;

        private Artifact relocatedArtifact;
    }

}
