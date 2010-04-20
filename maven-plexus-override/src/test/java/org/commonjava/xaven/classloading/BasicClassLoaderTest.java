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

package org.commonjava.xaven.classloading;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class BasicClassLoaderTest
{

    @Test
    public void loadClassLoaderResourceFromFileInClasspath()
        throws IOException
    {
        final File tmp = File.createTempFile( "resource.", ".txt" );
        tmp.deleteOnExit();

        FileWriter writer = null;
        try
        {
            writer = new FileWriter( tmp );
            new PrintWriter( writer ).println( "This is a test" );
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close();
                }
                catch ( final IOException e )
                {
                }
            }
        }

        final URLClassLoader ucl =
            new URLClassLoader( new URL[] { tmp.toURI().toURL() }, Thread.currentThread().getContextClassLoader() );

        assertNotNull( ucl.getResource( tmp.getName() ) );
    }

    @Test
    public void loadClassLoaderResourceFromJarInClasspathUsesRelativeResourcePath()
        throws IOException
    {
        final File tmp = File.createTempFile( "resource.", ".jar" );
        tmp.deleteOnExit();

        JarOutputStream jos = null;
        try
        {
            jos = new JarOutputStream( new FileOutputStream( tmp ) );
            final JarEntry entry = new JarEntry( "resource.txt" );
            jos.putNextEntry( entry );

            new PrintStream( jos ).println( "This is a test" );
            jos.closeEntry();
        }
        finally
        {
            if ( jos != null )
            {
                try
                {
                    jos.close();
                }
                catch ( final IOException e )
                {
                }
            }
        }

        final URLClassLoader ucl =
            new URLClassLoader( new URL[] { tmp.toURL() }, Thread.currentThread().getContextClassLoader() );

        assertNotNull( ucl.getResource( "resource.txt" ) );
        assertNull( ucl.getResource( "/resource.txt" ) );
    }
}
