package io.joshworks.snappy.rest;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public interface ErrorHandler<T extends Exception> {

    void onException(T e, RestExchange exchange);
}
