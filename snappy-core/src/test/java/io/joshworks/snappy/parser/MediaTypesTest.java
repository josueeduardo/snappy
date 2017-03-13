package io.joshworks.snappy.parser;

import io.joshworks.snappy.rest.MediaType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/13/17.
 */
public class MediaTypesTest {

    @Test
    public void withCharset() throws Exception {
        String type = "application/json; charset=UTF-8";
        MediaType result = MediaType.valueOf(type);
        assertEquals("application", result.getType());
        assertEquals("json", result.getSubtype());
        assertEquals(1, result.getParameters().size());
        assertEquals("UTF-8", result.getParameters().get("charset"));
        assertEquals(type, result.toString());
    }

    @Test
    public void multipleParameters() throws Exception {
        String type = "application/json; charset=UTF-8 q=1 qs=2";
        MediaType result = MediaType.valueOf(type);
        assertEquals("application", result.getType());
        assertEquals("json", result.getSubtype());
        assertEquals(3, result.getParameters().size());
        assertEquals("UTF-8", result.getParameters().get("charset"));
        assertEquals("1", result.getParameters().get("q"));
        assertEquals("2", result.getParameters().get("qs"));
        assertEquals(type, result.toString());
    }

    @Test
    public void withoutCharset() throws Exception {
        String type = "application/json";
        MediaType result = MediaType.valueOf(type);
        assertEquals("application", result.getType());
        assertEquals("json", result.getSubtype());
        assertEquals(type, result.toString());
    }

    @Test
    public void mimeMapping() throws Exception {
        String type = "json";
        MediaType result = MediaType.valueOf(type);
        assertEquals("application", result.getType());
        assertEquals("json", result.getSubtype());
        assertEquals("application/json", result.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMime() throws Exception {
        String type = "invalid123";
        MediaType.valueOf(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleSlash() throws Exception {
        String type = "invalid//123";
        MediaType.valueOf(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleSlash_beginning() throws Exception {
        String type = "/invalid/123";
        MediaType.valueOf(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleSlash_end() throws Exception {
        String type = "invalid/123/";
        MediaType.valueOf(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullMime() throws Exception {
        MediaType.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyMime() throws Exception {
        MediaType.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whitespaceMime() throws Exception {
        MediaType.valueOf(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void forwardSlash() throws Exception {
        MediaType.valueOf("/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void forwardSlashs() throws Exception {
        MediaType.valueOf("a////s");
    }

    @Test
    public void subtypeWildcard() throws Exception {
        String type = "text/*";
        MediaType result = MediaType.valueOf(type);
        assertEquals("text", result.getType());
        assertEquals("*", result.getSubtype());

    }

}