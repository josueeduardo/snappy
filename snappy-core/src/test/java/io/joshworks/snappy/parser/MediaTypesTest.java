package io.joshworks.snappy.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/13/17.
 */
public class MediaTypesTest {

    @Test
    public void withCharset() throws Exception {
        String type = "application/json";
        String result = MediaTypes.removeCharset(type + "; charset=UTF-8");
        assertEquals(type, result);
    }

    @Test
    public void withoutCharset() throws Exception {
        String type = "application/json";
        String result = MediaTypes.removeCharset(type);
        assertEquals(type, result);
    }

}