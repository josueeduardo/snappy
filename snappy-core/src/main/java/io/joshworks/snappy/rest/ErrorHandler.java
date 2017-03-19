package io.joshworks.snappy.rest;

import io.joshworks.snappy.Exchange;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public interface ErrorHandler<T extends Exception> {

    void onException(T e, Exchange exchange);
}
