package io.joshworks.snappy.parser;

import com.google.gson.Gson;
import io.joshworks.snappy.rest.MediaType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by josh on 3/6/17.
 */
public class JsonParser implements Parser {

    private final Gson gson = new Gson();

    @Override
    public <T> T readValue(String value, Class<T> valueType) throws Exception {
        return gson.fromJson(value, valueType);
    }

    @Override
    public String writeValue(Object input) throws Exception {
        return gson.toJson(input);
    }

    @Override
    public Set<MediaType> mediaTypes() {
        return new HashSet<>(Arrays.asList(MediaType.APPLICATION_JSON_TYPE));
    }
}
