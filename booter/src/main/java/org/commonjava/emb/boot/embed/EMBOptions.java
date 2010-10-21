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

package org.commonjava.emb.boot.embed;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.commonjava.emb.conf.EMBConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;

/**
 * 
 */
public interface EMBOptions
{

    public abstract EMBOptions withCoreClassLoader( final ClassLoader classLoader );

    public abstract EMBOptions withCoreClassLoader( final ClassLoader root, final Object... constituents )
        throws MalformedURLException;

    public abstract EMBOptions withClassWorld( final ClassWorld classWorld );

    public abstract ClassLoader coreClassLoader();

    public abstract ClassWorld classWorld();

    public abstract EMBOptions withContainerConfiguration( final ContainerConfiguration containerConfiguration );

    public abstract ContainerConfiguration containerConfiguration();

    public abstract EMBOptions withClassScanningEnabled( final boolean classScanningEnabled );

    public abstract EMBOptions withEMBConfiguration( final EMBConfiguration config );

    public abstract EMBConfiguration embConfiguration();

    public abstract EMBOptions withoutServiceLibraryLoader();

    public abstract EMBOptions withVersion( final boolean showVersion );

    public abstract boolean showVersion();

    public abstract EMBOptions withLogFile( final File logFile );

    public abstract File logFile();

    public abstract EMBOptions withQuietMode( final boolean quiet );

    public abstract boolean shouldBeQuiet();

    public abstract EMBOptions withDebugMode( final boolean debug );

    public abstract boolean shouldShowDebug();

    public abstract EMBOptions withErrorMode( final boolean showErrors );

    public abstract boolean shouldShowErrors();

    public abstract EMBOptions withStandardOut( final PrintStream stdout );

    public abstract PrintStream standardOut();

    public abstract EMBOptions withStandardErr( final PrintStream stderr );

    public abstract PrintStream standardErr();

    public abstract EMBOptions withStandardIn( final InputStream stdin );

    public abstract InputStream standardIn();

    public abstract String mavenHome();

    public abstract void withDebugLogHandles( final String[] debugLogHandles );

    public abstract String[] debugLogHandles();

}