package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.util.HeaderValues;

import java.util.stream.Collectors;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
class ConnegException extends Exception {

    final HeaderValues headerValues;
    final MediaTypes types;

    ConnegException(String message, HeaderValues headerValues, MediaTypes mediaTypes) {
        super(message);
        this.headerValues = headerValues;
        this.types = mediaTypes;
    }

    static ConnegException unsuportedMediaType(HeaderValues headerValues, MediaTypes types) {
        String typesString = types.stream().map(MediaType::toString).collect(Collectors.joining(", "));
        return new ConnegException(typesString, headerValues, types);
    }
}
