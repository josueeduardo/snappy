package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.util.HeaderValues;

import java.util.stream.Collectors;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class UnsupportedMediaType extends Exception {

    private static final String MESSAGE_PREFIX = "Unsupported media type: ";
    final HeaderValues headerValues;
    final MediaTypes types;

    private UnsupportedMediaType(String message, HeaderValues headerValues, MediaTypes mediaTypes) {
        super(message);
        this.headerValues = headerValues;
        this.types = mediaTypes;
    }

    public static UnsupportedMediaType unsuportedMediaType(HeaderValues headerValues, MediaTypes types) {
        String typesString = types.stream().map(MediaType::toString).collect(Collectors.joining(", "));
        typesString = "[" + typesString + "]";
        return new UnsupportedMediaType(MESSAGE_PREFIX + typesString, headerValues, types);
    }
}
