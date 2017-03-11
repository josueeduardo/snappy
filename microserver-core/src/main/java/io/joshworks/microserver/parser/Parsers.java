package io.joshworks.microserver.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by josh on 3/6/17.
 */
public class Parsers {

    private static final Map<String, Parser> available = new HashMap<>();

    static {
        Parsers.register("application/json", new JsonParser());
        Parsers.register("text/plain", new PlainTextParser());
        Parsers.register("*/*", new JsonParser());
    }

    private Parsers() {

    }

    public static void register(String mediaType, Parser parser) {
        available.put(mediaType, parser);
    }

    public static Parser find(List<String> types) {
        if(types.isEmpty()) {
            return available.get("*/*");
        }
        String[] triedTypes = new String[types.size()];
        int idx = 0;
        for (String type : types) {
            Parser parser = available.get(type);
            if (parser != null) {
                return parser;
            }
            triedTypes[idx++] = type;
        }
        throw new ParseNotFoundException(Arrays.toString(triedTypes));
    }

    public static Parser getParser(String contentType) {
        return available.get(contentType);
    }

}
