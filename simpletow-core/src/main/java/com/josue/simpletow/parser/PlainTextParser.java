package com.josue.simpletow.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by josh on 3/6/17.
 */
public class PlainTextParser implements Parser {

    @Override
    public String write(Object input) throws ParseException {
        return String.valueOf(input);
    }

    @Override
    public <T> T read(InputStream is, Class<T> type) throws ParseException {
        throw new UnsupportedOperationException("Cannot convert " + mediaType() + " to " + type);
    }

    @Override
    public <T> T read(String data, Class<T> type) throws ParseException {
        throw new UnsupportedOperationException("Cannot convert " + mediaType() + " to " + type);
    }

    @Override
    public String read(InputStream is) throws ParseException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new ParseException(ex);
        }

    }

    @Override
    public void stream(Object output, OutputStream os) throws ParseException {
        if (output == null) {
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os))) {
            bw.write(output.toString());
        } catch (Exception ex) {
            throw new ParseException(ex);
        }
    }

    @Override
    public String mediaType() {
        return "text/plain";
    }
}
