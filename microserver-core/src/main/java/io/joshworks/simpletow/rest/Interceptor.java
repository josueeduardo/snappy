package io.joshworks.simpletow.rest;

/**
 * Created by josh on 3/7/17.
 */
public interface Interceptor {

    void handleRequest(RestExchange exchange);
}
