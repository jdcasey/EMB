/*
 * Copyright 2010 Red Hat, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commonjava.emb.version.autobox;

import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.commonjava.emb.conf.ext.ExtensionConfiguration;
import org.commonjava.emb.version.autobox.lib.AutoboxingConfig;
import org.sonatype.aether.RepositoryEvent.EventType;
import org.sonatype.aether.RepositoryListener;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.MetadataResolver;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.metadata.Metadata;
import org.sonatype.aether.repository.ArtifactRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.resolution.MetadataRequest;
import org.sonatype.aether.resolution.MetadataResult;
import org.sonatype.aether.resolution.VersionRangeRequest;
import org.sonatype.aether.resolution.VersionRangeResolutionException;
import org.sonatype.aether.resolution.VersionRangeResult;
import org.sonatype.aether.util.listener.DefaultRepositoryEvent;
import org.sonatype.aether.util.metadata.DefaultMetadata;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionConstraint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component( role = VersionRangeResolver.class, hint = AutoboxingVersionRangeResolver.HINT )
public class AutoboxingVersionRangeResolver
    implements VersionRangeResolver
{

    public static final String HINT = "autoboxing";

    @Requirement
    private MetadataResolver resolver;

    @Requirement( role = ExtensionConfiguration.class, hint = "abx" )
    private AutoboxingConfig config;

    private transient AutoboxableVersionScheme scheme;

    public String formatRebuildVersion( final String baseVersion, final int rebuildNumber )
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersion v = (AutoboxableVersion) scheme().parseVersion( baseVersion );
        return String.valueOf( v.createRebuildVersion( rebuildNumber ) );
    }

    public boolean isRebuildVersion( final String version )
        throws InvalidVersionSpecificationException
    {
        final AutoboxableVersion v = (AutoboxableVersion) scheme().parseVersion( version );
        return v.isRebuild();
    }

    public VersionRangeResult resolveVersionRange( final RepositorySystemSession session,
                                                   final VersionRangeRequest request )
        throws VersionRangeResolutionException
    {
        return resolveVersionRange( session, request, true );
    }

    public VersionRangeResult resolveVersionRange( final RepositorySystemSession session,
                                                   final VersionRangeRequest request, final boolean defaultToOriginal )
        throws VersionRangeResolutionException
    {
        final VersionRangeResult result = new VersionRangeResult( request );
        final String spec = request.getArtifact().getVersion();

        VersionConstraint constraint;
        try
        {
            // CANNOT be null && MUST contain at least one range.
            constraint = scheme().parseVersionConstraint( spec );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            result.addException( e );
            throw new VersionRangeResolutionException( result, "Failed to parse version range for: "
                            + request.getArtifact() );
        }

        result.setVersionConstraint( constraint );
        resolveVersions( session, result, request, constraint,
                         defaultToOriginal && AutoboxingParser.isSingleVersion( spec ) );

        return result;
    }

    private synchronized AutoboxableVersionScheme scheme()
    {
        if ( scheme == null )
        {
            scheme = new AutoboxableVersionScheme( config );
        }

        return scheme;
    }

    private void resolveVersions( final RepositorySystemSession session, final VersionRangeResult result,
                                  final VersionRangeRequest req, final VersionConstraint constraint,
                                  final boolean defaultToOriginalVersion )
    {
        final Metadata md =
            new DefaultMetadata( req.getArtifact().getGroupId(), req.getArtifact().getArtifactId(),
                                 "maven-metadata.xml", Metadata.Nature.RELEASE_OR_SNAPSHOT );

        final List<MetadataRequest> reqs = new ArrayList<MetadataRequest>( req.getRepositories().size() );
        final AutoboxableVersionScheme scheme = scheme();

        reqs.add( new MetadataRequest( md, null, req.getRequestContext() ) );
        for ( final RemoteRepository repo : req.getRepositories() )
        {
            reqs.add( new MetadataRequest( md, repo, req.getRequestContext() ) );
        }

        final List<MetadataResult> results = resolver.resolveMetadata( session, reqs );

        final List<Version> resolved = new ArrayList<Version>();
        final Set<String> seen = new HashSet<String>();

        final WorkspaceReader workspace = session.getWorkspaceReader();
        if ( workspace != null )
        {
            final List<String> versions = workspace.findVersions( req.getArtifact() );
            for ( final String version : versions )
            {
                if ( !seen.contains( version ) )
                {
                    try
                    {
                        final Version ver = scheme().parseVersion( version );
                        if ( constraint.containsVersion( ver ) )
                        {
                            resolved.add( ver );
                            result.setRepository( ver, workspace.getRepository() );
                        }
                    }
                    catch ( final InvalidVersionSpecificationException e )
                    {
                        result.addException( e );
                    }
                }
            }
        }

        for ( final MetadataResult mResult : results )
        {
            if ( mResult.getException() != null )
            {
                result.addException( mResult.getException() );
            }

            ArtifactRepository repo = mResult.getRequest().getRepository();

            if ( repo == null )
            {
                repo = session.getLocalRepository();
            }

            final Versioning versioning = resolveVersions( session, mResult.getMetadata(), repo, result );

            for ( final String v : versioning.getVersions() )
            {
                if ( !seen.contains( v ) )
                {
                    try
                    {
                        final Version ver = scheme.parseVersion( v );
                        if ( constraint.containsVersion( ver ) )
                        {
                            resolved.add( ver );
                            result.setRepository( ver, repo );
                        }
                    }
                    catch ( final InvalidVersionSpecificationException e )
                    {
                        result.addException( e );
                    }
                }
            }
        }

        if ( resolved.isEmpty() && defaultToOriginalVersion )
        {
            try
            {
                resolved.add( scheme.parseVersion( req.getArtifact().getVersion() ) );
            }
            catch ( final InvalidVersionSpecificationException e )
            {
                result.addException( e );
            }
        }

        Collections.sort( resolved );
        result.setVersions( resolved );
    }

    private Versioning resolveVersions( final RepositorySystemSession session, final Metadata metadata,
                                        final ArtifactRepository repository, final VersionRangeResult result )
    {
        Versioning versioning = null;

        FileInputStream stream = null;
        try
        {
            if ( metadata != null && metadata.getFile() != null )
            {
                stream = new FileInputStream( metadata.getFile() );
                versioning = new MetadataXpp3Reader().read( stream, false ).getVersioning();
            }
        }
        catch ( final IOException e )
        {
            if ( !( e instanceof FileNotFoundException ) )
            {
                sendInvalidEvent( session, metadata, repository, e );
                result.addException( e );
            }
        }
        catch ( final XmlPullParserException e )
        {
            sendInvalidEvent( session, metadata, repository, e );
            result.addException( e );
        }
        finally
        {
            IOUtil.close( stream );
        }

        return ( versioning != null ) ? versioning : new Versioning();
    }

    private void sendInvalidEvent( final RepositorySystemSession session, final Metadata metadata,
                                   final ArtifactRepository repository, final Exception exception )
    {
        final RepositoryListener listener = session.getRepositoryListener();
        if ( listener != null )
        {
            listener.metadataInvalid( new DefaultRepositoryEvent( EventType.METADATA_INVALID, session ).setException( exception )
                                                                                                       .setMetadata( metadata )
                                                                                                       .setRepository( repository ) );
        }
    }

}
