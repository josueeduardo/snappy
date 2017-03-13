package io.joshworks.snappy.parser;

/**
 * Created by josh on 3/6/17.
 */
public class ParseNotFoundException extends RuntimeException {

    public ParseNotFoundException(String mediaType) {
        super("Parser not found for media type '" + mediaType + "'");
    }
}
