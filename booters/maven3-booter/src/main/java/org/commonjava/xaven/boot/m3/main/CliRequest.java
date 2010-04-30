package org.commonjava.xaven.boot.m3.main;

import org.apache.commons.cli.CommandLine;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.commonjava.xaven.DefaultXavenExecutionRequest;
import org.commonjava.xaven.XavenExecutionRequest;
import org.commonjava.xaven.boot.m3.embed.XavenEmbedderBuilder;

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

    public String workingDirectory;

    public PrintStream fileStream;

    public Properties userProperties = new Properties();

    public Properties systemProperties = new Properties();

    public XavenExecutionRequest request;

    public XavenEmbedderBuilder builder;

    public CliRequest( final String[] args, final ClassWorld classWorld )
    {
        this.args = args;
        builder = new XavenEmbedderBuilder().withClassWorld( classWorld );
        request = new DefaultXavenExecutionRequest();
    }

}