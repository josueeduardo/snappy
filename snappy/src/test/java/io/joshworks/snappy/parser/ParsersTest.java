package io.joshworks.snappy.parser;

import io.joshworks.snappy.parser.parser.ExactTypeParser;
import io.joshworks.snappy.parser.parser.SubTypeWildcardParser;
import io.joshworks.snappy.parser.parser.WildcardParser;
import io.joshworks.snappy.http.MediaType;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Josh Gontijo on 4/30/17.
 */
public class ParsersTest {

    @Before
    public void cleanup() {
        Parsers.available.clear();
        Parsers.mostSpecificOrderedParsers.clear();
    }

    @Test
    public void mostCompatibleFirst() throws Exception {
        ExactTypeParser exact = new ExactTypeParser();
        SubTypeWildcardParser subTypeWildcard = new SubTypeWildcardParser();
        WildcardParser wildcard = new WildcardParser();

        Parsers.register(subTypeWildcard);
        Parsers.register(wildcard);
        Parsers.register(exact);//register as last

        Parser parser = Parsers.getParser(MediaType.TEXT_PLAIN_TYPE);
        assertEquals(exact, parser);
    }
}