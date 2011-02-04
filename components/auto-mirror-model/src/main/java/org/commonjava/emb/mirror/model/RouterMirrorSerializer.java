package org.commonjava.emb.mirror.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouterMirrorSerializer
{

    private static Gson gson;

    public static void serialize( final RouterMirrorsMapping map, final Writer writer )
    {
        getGson().toJson( map, writer );
    }

    public static String serializeToString( final RouterMirrorsMapping map )
    {
        return getGson().toJson( map );
    }

    public static RouterMirrorsMapping deserialize( final Reader reader )
    {
        return getGson().fromJson( reader, RouterMirrorsMapping.class );
    }

    public static RouterMirrorsMapping deserialize( final String source )
    {
        return getGson().fromJson( source, RouterMirrorsMapping.class );
    }

    private static Gson getGson()
    {
        if ( gson == null )
        {
            gson =
                new GsonBuilder().disableHtmlEscaping()
                                 .disableInnerClassSerialization()
                                 .setPrettyPrinting()
                                 .registerTypeAdapter( new TypeToken<List<RouterMirror>>()
                                 {
                                 }.getType(), new RepositoryMirrorListCreator() )
                                 .registerTypeAdapter( new TypeToken<Map<String, RouterMirrors>>()
                                 {
                                 }.getType(), new RepositoryMirrorCollectionMapCreator() )
                                 .create();
        }

        return gson;
    }

    public static final class RepositoryMirrorCollectionMapCreator
        implements InstanceCreator<Map<String, RouterMirrors>>
    {
        @Override
        public Map<String, RouterMirrors> createInstance( final Type type )
        {
            return new HashMap<String, RouterMirrors>();
        }
    }

    public static final class RepositoryMirrorListCreator
        implements InstanceCreator<List<RouterMirror>>
    {

        @Override
        public List<RouterMirror> createInstance( final Type type )
        {
            return new ArrayList<RouterMirror>();
        }

    }

}
