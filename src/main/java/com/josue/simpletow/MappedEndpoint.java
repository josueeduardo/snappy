package com.josue.simpletow;

/**
 * Created by josh on 3/7/17.
 */
public class MappedEndpoint {

    final String prefix; //HTTP methods or WS etc
    final String url;

    public MappedEndpoint(String prefix, String url) {
        this.prefix = prefix;
        this.url = url;
    }
}
