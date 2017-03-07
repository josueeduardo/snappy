package com.josue.simpletow.parser;

import io.undertow.util.HeaderValues;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by josh on 3/6/17.
 */
public class Parsers {

    private Parsers(){}

    private static final Map<String, Parser> available = new HashMap<>();

    public static void register(String mediaType, Parser parser) {
        available.put(mediaType, parser);
    }

    public static Parser getParser(HeaderValues types) {
        String[] triedTypes = new String[types.size()];
        int idx = 0;
        for(String type : types) {
            Parser parser = available.get(type);
            if(parser != null) {
                return parser;
            }
            triedTypes[idx++] = type;
        }
        throw new ParseNotFoundException(Arrays.toString(triedTypes));
    }

}
