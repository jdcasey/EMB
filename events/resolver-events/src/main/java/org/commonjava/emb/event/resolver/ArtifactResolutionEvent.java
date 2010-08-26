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

package org.commonjava.emb.event.resolver;

import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.commonjava.emb.event.EMBEvent;

public class ArtifactResolutionEvent
    extends EMBEvent
    implements ResolutionEvent
{

    private final ArtifactResolutionRequest request;

    private final ResolutionEventType type;

    private final ArtifactResolutionResult result;

    public ArtifactResolutionEvent( final ArtifactResolutionRequest request )
    {
        this.request = request;
        result = null;
        type = ResolutionEventType.START;
    }

    public ArtifactResolutionEvent( final ArtifactResolutionRequest request, final ArtifactResolutionResult result )
    {
        this.request = request;
        this.result = result;
        type = result.hasExceptions() ? ResolutionEventType.FAIL : ResolutionEventType.SUCCESS;
    }

    public ArtifactResolutionResult getResult()
    {
        return result;
    }

    public ArtifactResolutionRequest getRequest()
    {
        return request;
    }

    public ResolutionEventType getType()
    {
        return type;
    }

}
