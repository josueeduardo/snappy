package io.joshworks.snappy.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by josh on 3/6/17.
 */
public class Parsers {

    private static final Logger logger = LoggerFactory.getLogger(Parsers.class);

    private static final Map<String, Parser> available = new HashMap<>();
    private static final Parser fallback = new PlainTextParser();

    static {
        Parsers.register(new JsonParser());
        Parsers.register(new PlainTextParser());
    }

    private Parsers() {

    }

    public static void register(Parser parser) {
        if (parser == null || parser.mediaTypes() == null || parser.mediaTypes().isEmpty()) {
            throw new RuntimeException("Invalid parser");
        }
        stripTypes(parser).forEach(t -> available.put(t, parser));
    }

    private static Set<String> stripTypes(Parser parser) {
        Set<String> types = new HashSet<>();
        for (String type : parser.mediaTypes()) {
            type = MediaTypes.removeCharset(type);
            if (type != null) {
                types.add(type);
            }
        }
        return types;

    }


    //TODO fallback to plain text
    public static Parser find(List<String> types) {
        if (types.isEmpty()) {
            return fallback;
        }
        String[] triedTypes = new String[types.size()];
        int idx = 0;
        for (String type : types) {
            type = MediaTypes.removeCharset(type);
            Parser parser = available.get(type);
            if (parser != null) {
                return parser;
            }
            triedTypes[idx++] = type;
        }
        logger.warn("Parser not found media types {}", Arrays.toString(triedTypes));
        return fallback;
    }

    public static Parser getParser(String contentType) {
        return available.get(contentType);
    }

}
