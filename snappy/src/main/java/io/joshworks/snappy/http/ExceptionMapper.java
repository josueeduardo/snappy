/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.http;

import io.joshworks.snappy.handler.UnsupportedMediaType;
import io.undertow.util.StatusCodes;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class ExceptionMapper extends ConcurrentHashMap<Class<? extends Exception>, ErrorHandler> {

    private static final ErrorHandler<Exception> fallbackInternalError = (e, restExchange) -> {
        restExchange.status(StatusCodes.INTERNAL_SERVER_ERROR);
        restExchange.send(ExceptionResponse.of(e), MediaType.APPLICATION_JSON_TYPE);
    };

    private static final ErrorHandler<HttpException> httpException = (e, restExchange) -> {
        restExchange.status(e.status);
        restExchange.send(ExceptionResponse.of(e), MediaType.APPLICATION_JSON_TYPE);
    };

    private static final ErrorHandler<Exception> fallbackConneg = (e, restExchange) -> {
        int status = StatusCodes.UNSUPPORTED_MEDIA_TYPE;
        restExchange.status(status);
        restExchange.send(ExceptionResponse.of(e), MediaType.APPLICATION_JSON_TYPE);
    };

    public ExceptionMapper() {
        put(Exception.class, fallbackInternalError);
        put(HttpException.class, httpException);
        put(UnsupportedMediaType.class, fallbackConneg);
    }

    public <T extends Exception> ErrorHandler<T> getOrFallback(T key) {
        return this.getOrFallback(key, fallbackInternalError);
    }

    public <T extends Exception> ErrorHandler<T> getOrFallback(T key, ErrorHandler fallback) {
        ErrorHandler<T> errorHandler = super.get(key.getClass());
        if (errorHandler == null) {
            Optional<Entry<Class<? extends Exception>, ErrorHandler>> found = entrySet().stream()
                    .filter(e -> e.getKey().isAssignableFrom(key.getClass()))
                    .findFirst();

            errorHandler = found.isPresent() ? found.get().getValue() : fallback;
        }
        return errorHandler;
    }

}
