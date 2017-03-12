package io.joshworks.microserver.parser;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/12/17.
 */
public class ParsersTest {


    @Test
    public void withCharset() throws Exception {
        String type = "application/json";
        String result = Parsers.stripTypes(type + "; charset=UTF-8");
        assertEquals(type, result);

    }

    @Test
    public void withoutCharset() throws Exception {
        String type = "application/json";
        String result = Parsers.stripTypes(type);
        assertEquals(type, result);

    }

    @Test
    public void find() throws Exception {

    }

    @Test
    public void getParser() throws Exception {

    }

    static class ParserA implements Parser {


        @Override
        public <T> T readValue(String value, Class<T> valueType) throws Exception {
            return null;
        }

        @Override
        public String writeValue(Object value) throws Exception {
            return null;
        }

        @Override
        public Set<String> mediaTypes() {
            return null;
        }
    }

}