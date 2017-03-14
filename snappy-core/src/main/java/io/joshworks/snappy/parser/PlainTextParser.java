package io.joshworks.snappy.parser;

import io.joshworks.snappy.rest.MediaType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by josh on 3/6/17.
 */
public class PlainTextParser implements Parser {

    @Override
    public <T> T readValue(String value, Class<T> valueType) throws Exception {
        throw new UnsupportedOperationException("Cannot convert " + Arrays.toString(mediaTypes().toArray()) + " to " + valueType);
    }

    @Override
    public String writeValue(Object input) throws ParseException {
        return String.valueOf(input);
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return new HashSet<>(Arrays.asList(MediaType.TEXT_PLAIN_TYPE));
    }

}
