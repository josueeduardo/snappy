package io.joshworks.snappy.rest;

import io.joshworks.snappy.handler.UnsupportedMediaType;
import io.undertow.util.StatusCodes;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class ExceptionMapper extends ConcurrentHashMap<Class<? extends Exception>, ErrorHandler> {

    private final ErrorHandler fallbackInternalError = (e, restExchange) -> {
        int status = StatusCodes.INTERNAL_SERVER_ERROR;
        restExchange.status(status);

        ExceptionResponse response = new ExceptionResponse(status, e.getMessage());
        restExchange.send(response, MediaType.APPLICATION_JSON_TYPE);
    };

    private final ErrorHandler fallbackConneg = (e, restExchange) -> {
        int status = StatusCodes.UNSUPPORTED_MEDIA_TYPE;
        restExchange.status(status);

        ExceptionResponse response = new ExceptionResponse(status, e.getMessage());
        restExchange.send(response, MediaType.APPLICATION_JSON_TYPE);
    };

    public ExceptionMapper() {
        put(Exception.class, fallbackInternalError);
        put(UnsupportedMediaType.class, fallbackConneg);
    }

    public <T extends Exception> ErrorHandler<T> getOrFallback(T key) {
        ErrorHandler<T> errorHandler = super.get(key.getClass());
        if (errorHandler == null) {
            Optional<Entry<Class<? extends Exception>, ErrorHandler>> found = entrySet().stream()
                    .filter(e -> e.getKey().isAssignableFrom(key.getClass()))
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
