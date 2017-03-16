package io.joshworks.snappy.parser;

import io.joshworks.snappy.rest.MediaType;

/**
 * Created by josh on 3/6/17.
 */
public class PlainTextParser implements Parser {

    @Override
    public <T> T readValue(String value, Class<T> valueType) throws Exception {
        throw new UnsupportedOperationException("Cannot convert " + valueType + " to " + mediaType());
    }

    @Override
    public String writeValue(Object input) throws ParseException {
        return String.valueOf(input);
    }

    @Override
    public MediaType mediaType() {
        return MediaType.TEXT_PLAIN_TYPE;
    }

}
