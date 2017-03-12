package io.joshworks.microserver.parser;

import java.util.Set;

/**
 * Created by josh on 3/6/17.
 */
public interface Parser {

    <T> T readValue(String value, Class<T> valueType) throws Exception;

    String writeValue(Object value) throws Exception;

    public Set<String> mediaTypes();
}
