/*
 * Copyright (c) 2010 Red Hat, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see 
 * <http://www.gnu.org/licenses>.
 */

package org.commonjava.emb.version.resolve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.maven.project.MavenProject;
import org.commonjava.emb.version.resolve.testutil.TestFixture;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ProjectManagerTest
{

    private static TestFixture fixture;

    @Test
    public void retrieveReactorProjects()
        throws Exception
    {
        final ProjectManager projectManager = fixture.projectManager();

        final File dir = fixture.getTestFile( "projects", "multi-module" );
        final MavenProject project =
            projectManager.buildProjectInstance( new File( dir, "pom.xml" ), fixture.newSession() );

        final Set<String> projectIds = projectManager.retrieveReactorProjectIds( project.getFile() );

        System.out.println( projectIds );

        assertTrue( "parent project missing", projectIds.contains( "test:parent:1" ) );
        assertTrue( "child1 project missing", projectIds.contains( "test:child1:1" ) );
        assertTrue( "child2 project missing", projectIds.contains( "test:child2:1" ) );
    }

    @Test
    public void buildInstanceFromFile()
        throws Exception
    {
        final File pom = fixture.getTestFile( "projects", "simple.pom.xml" );
        final MavenProject project = fixture.projectManager().buildProjectInstance( pom, fixture.newSession() );

        assertEquals( pom, project.getFile() );
        assertEquals( "test", project.getGroupId() );
        assertEquals( "project", project.getArtifactId() );
        assertEquals( "1", project.getVersion() );
    }

    @Test
    public void buildInstanceFromCoords()
        throws Exception
    {
        final MavenProject project =
            fixture.projectManager().buildProjectInstance( "test", "found-dep", "1", fixture.newSession() );

        assertEquals( "test", project.getGroupId() );
        assertEquals( "found-dep", project.getArtifactId() );
        assertEquals( "1", project.getVersion() );
    }

    @BeforeClass
    public static void setup()
        throws Exception
    {
        fixture = TestFixture.getInstance();
    }

    @AfterClass
    public static void shutdown()
        throws IOException
    {
        if ( fixture != null )
        {
            fixture.shutdown();
        }
    }

}
