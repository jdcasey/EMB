package org.commonjava.xaven.boot.m3;

import org.commonjava.xaven.conf.AbstractXavenLibrary;
import org.commonjava.xaven.conf.MavenPomVersionProvider;

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

public class Maven3BooterLibrary
    extends AbstractXavenLibrary
{
    public Maven3BooterLibrary()
    {
        super( "core", "Xaven-Core", new MavenPomVersionProvider( "org.commonjava.xaven.boot", "xaven-maven3-booter" ) );
    }
}
