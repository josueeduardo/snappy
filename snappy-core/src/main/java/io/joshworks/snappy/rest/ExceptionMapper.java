package io.joshworks.snappy.rest;

import io.joshworks.snappy.handler.UnsupportedMediaType;
import io.undertow.util.StatusCodes;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class ExceptionMapper extends ConcurrentHashMap<Class<? extends Exception>, ErrorHandler> {

    private final ErrorHandler fallbackInternalError = (e) -> {
        int status = StatusCodes.INTERNAL_SERVER_ERROR;
        DefaultExceptionResponse response = new DefaultExceptionResponse(status, e.getMessage());
        return new ExceptionResponse(status, response);
    };

    private final ErrorHandler fallbackConneg = (e) -> {
        int status = StatusCodes.UNSUPPORTED_MEDIA_TYPE;
        DefaultExceptionResponse response = new DefaultExceptionResponse(status, e.getMessage());
        return new ExceptionResponse(status, response);
    };

    public ExceptionMapper() {
        put(Exception.class, fallbackInternalError);
        put(UnsupportedMediaType.class, fallbackConneg);
    }

    public ErrorHandler getOrFallback(Class<? extends Exception> key) {
        ErrorHandler errorHandler = super.get(key);
        if (errorHandler == null) {
            Optional<Entry<Class<? extends Exception>, ErrorHandler>> found = entrySet().stream()
                    .filter(e -> e.getKey().isAssignableFrom(key))
                    .findFirst();

            errorHandler = found.isPresent() ? found.get().getValue() : fallbackInternalError;
        }
        return errorHandler;
    }

    public ErrorHandler getFallbackInternalError() {
        return fallbackInternalError;
    }

    public ErrorHandler getFallbackConneg() {
        return fallbackConneg;
    }
}
