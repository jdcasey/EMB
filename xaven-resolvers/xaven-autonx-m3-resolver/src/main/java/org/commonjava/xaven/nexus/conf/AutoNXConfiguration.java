package org.commonjava.xaven.nexus.conf;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.http.auth.UsernamePasswordCredentials;
import org.commonjava.xaven.conf.ext.ExtensionConfiguration;

public class AutoNXConfiguration
    implements ExtensionConfiguration
{

    public static final String DEFAULT_MIRROR_ID = "nexus";

    private String nexusUrl;

    private String mirrorId = DEFAULT_MIRROR_ID;

    private UsernamePasswordCredentials nexusCredentials;

    public AutoNXConfiguration withNexusCredentials( final String user, final String password )
    {
        nexusCredentials = new UsernamePasswordCredentials( user, password );
        return this;
    }

    public UsernamePasswordCredentials getNexusCredentials()
    {
        return nexusCredentials;
    }

    public AutoNXConfiguration withNexusUrl( final String nexusUrl )
    {
        this.nexusUrl = nexusUrl;
        return this;
    }

    public AutoNXConfiguration withMirrorId( final String mirrorId )
    {
        if ( mirrorId != null )
        {
            this.mirrorId = mirrorId;
        }

        return this;
    }

    public String getNexusUrl()
    {
        return nexusUrl;
    }

    public String getMirrorId()
    {
        return mirrorId;
    }

}
