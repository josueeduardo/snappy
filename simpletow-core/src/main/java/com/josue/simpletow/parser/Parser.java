package com.josue.simpletow.parser;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by josh on 3/6/17.
 */
public interface Parser {

    String write(Object input) throws ParseException;

    <T> T read(InputStream is, Class<T> type) throws ParseException;
    <T> T read(String data, Class<T> type) throws ParseException;

    String read(InputStream is) throws ParseException;



    void stream(Object output, OutputStream os) throws ParseException;

    String mediaType();
}
