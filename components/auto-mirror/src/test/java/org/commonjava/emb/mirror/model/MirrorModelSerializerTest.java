package org.commonjava.emb.mirror.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.StringWriter;

public class MirrorModelSerializerTest
{

    @Test
    public void serializeMapWithOneCollectionWithOneMirror()
    {
        final RouterMirrorsMapping mirrorMap =
            new RouterMirrorsMapping().addMirror( "http://repo1.maven.org/maven2",
                                                 new RouterMirror( "central", "http://localhost:8081/nexus", 99,
                                                                       true ) );

        final StringWriter sw = new StringWriter();
        RouterMirrorSerializer.serialize( mirrorMap, sw );

        System.out.println( sw );
    }

    @Test
    public void serializeToStringMapWithOneCollectionWithOneMirror()
    {
        final RouterMirrorsMapping mirrorMap =
            new RouterMirrorsMapping().addMirror( "http://repo1.maven.org/maven2",
                                                 new RouterMirror( "central", "http://localhost:8081/nexus", 99,
                                                                       true ) );

        System.out.println( RouterMirrorSerializer.serializeToString( mirrorMap ) );
    }

    @Test
    public void roundTripMapWithOneCollectionWithOneMirror()
    {
        final RouterMirrorsMapping mirrorMap =
            new RouterMirrorsMapping().addMirror( "http://repo1.maven.org/maven2",
                                                 new RouterMirror( "central", "http://localhost:8081/nexus", 99,
                                                                       true ) );

        final String ser = RouterMirrorSerializer.serializeToString( mirrorMap );
        final RouterMirrorsMapping result = RouterMirrorSerializer.deserialize( ser );

        assertEquals( mirrorMap, result );
    }

}
