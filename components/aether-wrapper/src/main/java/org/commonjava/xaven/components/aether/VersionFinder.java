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

import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.aether.DefaultArtifact;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.VersionRangeRequest;

import java.util.List;

@Component( role = VersionFinder.class )
public class VersionFinder
{
    
    private static final String PLUGIN = "maven-plugin";
    
    @Requirement
    RepositorySystem repoSystem;
    
    public List<String> findPluginVersions( String groupId, String artifactId, VersionRange range )
    {
        VersionRangeRequest req = new VersionRangeRequest();
        req.setArtifact( new DefaultArtifact( groupId, artifactId, PLUGIN, null ) );
        req.setRepositories( Collection )
        
    }

}
