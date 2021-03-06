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

package org.commonjava.emb.conf.mgmt;

import org.commonjava.emb.EMBException;

public class EMBManagementException
    extends EMBException
{

    private static final long serialVersionUID = 1L;

    public EMBManagementException( final String message, final Object... params )
    {
        super( message, params );
    }

    public EMBManagementException( final String message, final Throwable cause, final Object... params )
    {
        super( message, cause, params );
    }

    public EMBManagementException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    public EMBManagementException( final String message )
    {
        super( message );
    }

}
