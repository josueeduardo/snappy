package io.joshworks.snappy.parser;

import io.joshworks.snappy.rest.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
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

    private static final Map<MediaType, Parser> available = new HashMap<>();
    private static final Parser defaultParser = new JsonParser();

    private Parsers() {

    }

    /**
     * @param parser The {@link Parser to be registered}
     * @throws IllegalArgumentException if a null instance or no media type is provided
     */
    public static void register(Parser parser) {
        if (parser == null || parser.mediaType() == null) {
            throw new IllegalArgumentException("Invalid parser, media type not specified, or null instance");
        }
        logger.info("Registering Parser {} for type {}", parser.getClass().getName(), parser.mediaType().toString());
        available.put(parser.mediaType(), parser);
    }

    /**
     * @param contentTypes The accept types by the client
     * @return The {@link Parser} for the first match, if no media type is provided, the default {@link JsonParser}
     */
    public static Parser find(List<String> contentTypes) throws ParseNotFoundException {
        List<MediaType> types = new ArrayList<>();
        for (String ct : contentTypes) {
            types.add(MediaType.valueOf(ct));
        }
        return findByType(new HashSet<>(types));
    }

    public static Parser findByType(Set<MediaType> contentTypes) throws ParseNotFoundException {
        if (contentTypes == null || contentTypes.isEmpty()) {
            return defaultParser;
        }
        for (Map.Entry<MediaType, Parser> parser : available.entrySet()) {
            for (MediaType acceptType : contentTypes) {
                if (parser.getKey().isCompatible(acceptType)) {
                    return parser.getValue();
                }
            }
        }
        throw new ParseNotFoundException(contentTypes.stream().map(MediaType::toString).toArray(String[]::new));
    }

    /**
     * @param contentType The accept types by the client
     * @return The {@link Parser} for the first match, if no media type is provided, the default {@link JsonParser}
     */
    public static Parser getParser(MediaType contentType) {
        return findByType(new HashSet<>(Collections.singletonList(contentType)));
    }

    public static Parser getParser(String contentType) {
        return getParser(MediaType.valueOf(contentType));
    }

}
