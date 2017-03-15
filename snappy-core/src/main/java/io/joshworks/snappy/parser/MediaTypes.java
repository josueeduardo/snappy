package io.joshworks.snappy.parser;

import io.joshworks.snappy.rest.MediaType;
import io.undertow.util.HeaderValues;

import java.util.HashSet;

/**
 * Created by josh on 3/12/17.
 */
public class MediaTypes extends HashSet<MediaType> {

    public enum Context {
        PRODUCES, CONSUMES

    }

    public static MediaTypes DEFAULT_CONSUMES = new MediaTypes(Context.CONSUMES, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);
    public static MediaTypes DEFAULT_PRODUCES = new MediaTypes(Context.CONSUMES, MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);


    private final Context context;

    private MediaTypes(Context context) {
        this.context = context;
    }

    private MediaTypes(Context context, String... types) {
        this(context);
        for (String mt : types) {
            add(MediaType.valueOf(mt));
        }
    }

    /**
     * @param header
     * @return The first match of the provided content Mime context
     */
    public MediaType match(HeaderValues header) {
        if(header != null) {
            for (String headerVal : header) {
                try {
                    MediaType mediaType = MediaType.valueOf(headerVal);
                    boolean match = this.stream().anyMatch(t -> t.isCompatible(mediaType));
                    if (match) {
                        return mediaType;
                    }
                } catch (Exception e) {
                    //ignore mime
                }
            }
        }
        return null;
    }


    public static MediaTypes consumes(String... types) {
        return new MediaTypes(Context.CONSUMES, types);
    }

    public static MediaTypes produces(String... types) {
        return new MediaTypes(Context.PRODUCES, types);
    }

    public Context getContext() {
        return context;
    }


}
