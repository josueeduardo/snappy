/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.parser;

import io.joshworks.snappy.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.joshworks.snappy.SnappyServer.LOGGER_NAME;

/**
 * Created by josh on 3/6/17.
 */
public class Parsers {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    static final Map<MediaType, Parser> available = new HashMap<>();

    private Parsers() {

    }

    /**
     * @param parser The {@link Parser to be registered}
     * @throws IllegalArgumentException if a null instance or no media type is provided
     */
    public static void register(MediaType mediaType, Parser parser) {
        Objects.requireNonNull(mediaType, "MediaType must be provided");
        Objects.requireNonNull(parser, "Parser must be provided");

        logger.info("Registering Parser '{}' for type {}", parser.getClass().getSimpleName(), mediaType.toString());
        available.put(mediaType, parser);
    }

    public static void clear() {
        available.clear();
    }

    /**
     * @param contentType The accepted types
     * @return The {@link Parser} for the first match.
     * @throws ParserNotFoundException If parser is not found
     */
    public static Parser getParser(MediaType contentType) throws ParserNotFoundException {
        if (contentType == null) {
            throw new ParserNotFoundException("Content type not specified");
        }
        return available.get(contentType);
    }

}
