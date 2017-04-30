package io.joshworks.snappy.parser.parser;

import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.rest.MediaType;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Josh Gontijo on 4/30/17.
 */
public class ExactTypeParser implements Parser {
    @Override
    public <T> T readValue(String value, Class<T> valueType) {
        return null;
    }

    @Override
    public <T> T readValue(String value, Type valueType) {
        return null;
    }

    @Override
    public String writeValue(Object value) {
        return null;
    }

    @Override
    public Set<MediaType> mediaType() {
        return new HashSet<>(Arrays.asList(MediaType.TEXT_PLAIN_TYPE));
    }
}
