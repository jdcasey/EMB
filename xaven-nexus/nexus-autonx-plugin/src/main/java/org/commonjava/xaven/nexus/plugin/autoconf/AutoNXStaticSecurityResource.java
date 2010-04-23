package org.commonjava.xaven.nexus.plugin.autoconf;

import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

import javax.inject.Named;

@Named( "autoNXSecurity" )
public class AutoNXStaticSecurityResource
    extends AbstractStaticSecurityResource
    implements StaticSecurityResource
{

    @Override
    protected String getResourcePath()
    {
        return "/META-INF/nexus/autonx-security.xml";
    }

}
