package org.commonjava.xaven.boot.m3.main;

import org.apache.commons.cli.CommandLine;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.commonjava.xaven.conf.XavenConfiguration;

import java.io.PrintStream;
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

public final class CliRequest
{
    public String[] args;

    public CommandLine commandLine;

    public PrintStream stdout;

    public PrintStream stderr;

    public ClassWorld classWorld;

    public String workingDirectory;

    public boolean debug;

    public boolean quiet;

    public boolean showErrors = true;

    public PrintStream fileStream;

    public Properties userProperties = new Properties();

    public Properties systemProperties = new Properties();

    public MavenExecutionRequest request;

    public XavenConfiguration xavenConfig;

    public CliRequest( final String[] args, final ClassWorld classWorld )
    {
        this.args = args;
        this.classWorld = classWorld;
        request = new DefaultMavenExecutionRequest();
    }

}
