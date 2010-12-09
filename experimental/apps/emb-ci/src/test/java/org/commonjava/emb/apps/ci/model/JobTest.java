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

package org.commonjava.emb.apps.ci.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class JobTest
{

    @Test
    public void serializeWithRepositorySource()
        throws Exception
    {
        final XStream xs = new XStream();
        xs.processAnnotations( Job.class );

        final Job job =
            new Job( "foo", new RepositorySource( "org.apache.maven", "maven", "3.0",
                                                  "http://localhost:8081/nexus/content/groups/public" ) );
        final String xml = xs.toXML( job );

        System.out.println( xml );
    }

    @Test
    public void serializeWithSCMSource()
        throws Exception
    {
        final XStream xs = new XStream();
        xs.processAnnotations( Job.class );

        final Job job = new Job( "foo", new SCMSource( "http://svn.apache.org/repos/asf/maven/maven-3/trunk" ) );

        final String xml = xs.toXML( job );

        System.out.println( xml );
    }

    @Test
    public void roundTripWithRepositorySource()
        throws Exception
    {
        final XStream xs = new XStream();
        xs.processAnnotations( Job.class );

        final RepositorySource source =
            new RepositorySource( "org.apache.maven", "maven", "3.0",
                                  "http://localhost:8081/nexus/content/groups/public" );

        final Job job = new Job( "foo", source );

        final String xml = xs.toXML( job );

        System.out.println( xml );

        final Job out = (Job) xs.fromXML( xml );

        assertEquals( job.getName(), out.getName() );

        final RepositorySource os = (RepositorySource) job.getProjectSource();
        assertEquals( source.getArtifactId(), os.getArtifactId() );
        assertEquals( source.getGroupId(), os.getGroupId() );
        assertEquals( source.getRepositoryUrl(), os.getRepositoryUrl() );
        assertEquals( source.getVersion(), os.getVersion() );
    }

    @Test
    public void roundTripWithSCMSource()
        throws Exception
    {
        final XStream xs = new XStream();
        xs.processAnnotations( Job.class );

        final SCMSource source = new SCMSource( "http://svn.apache.org/repos/asf/maven/maven-3/trunk" );
        final Job job = new Job( "foo", source );
        final String xml = xs.toXML( job );

        System.out.println( xml );

        final Job out = (Job) xs.fromXML( xml );

        assertEquals( job.getName(), out.getName() );

        final SCMSource os = (SCMSource) out.getProjectSource();
        assertEquals( source.getRootPomPath(), os.getRootPomPath() );
        assertEquals( source.getScmUrl(), os.getScmUrl() );
    }

}
