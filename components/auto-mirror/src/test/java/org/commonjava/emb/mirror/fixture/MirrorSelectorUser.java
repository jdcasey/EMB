package org.commonjava.emb.mirror.fixture;

import org.apache.maven.repository.MirrorSelector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = MirrorSelectorUser.class )
public class MirrorSelectorUser
{

    @Requirement
    private MirrorSelector mirrorSelector;

    public MirrorSelector mirrorSelector()
    {
        return mirrorSelector;
    }

}
