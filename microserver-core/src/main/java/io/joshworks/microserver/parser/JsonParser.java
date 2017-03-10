package io.joshworks.microserver.parser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

/**
 * Created by josh on 3/6/17.
 */
public class JsonParser implements Parser {

    private final Gson gson = new Gson();

    @Override
    public String write(Object input) {
        return gson.toJson(input);
    }


    @Override
    public <T> T read(InputStream is, Class<T> type) {
        return gson.fromJson(new BufferedReader(new InputStreamReader(is)), type);
    }

    @Override
    public <T> T read(String data, Class<T> type) throws ParseException {
        return gson.fromJson(new StringReader(data), type);
    }

    @Override
    public String read(InputStream is) throws ParseException {
        com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
        JsonElement parsed = parser.parse(new BufferedReader(new InputStreamReader(is)));
        return parsed == null ? null : parsed.toString();
    }

    @Override
    public void stream(Object output, OutputStream out) {
        gson.toJson(output, new BufferedWriter(new OutputStreamWriter(out)));
    }

    @Override
    public String mediaType() {
        return "application/json";
    }
}
