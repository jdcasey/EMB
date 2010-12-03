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

package org.commonjava.emb.boot.embed;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.commonjava.emb.conf.EMBConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;

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
