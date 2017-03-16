package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Josh Gontijo on 3/5/17.
 * <p>
 * This should be the higher level Rest class, ideally this is wrapped in the Error handler which will return
 * handler exceptions thrown by the endpoints
 */
public class RestDispatcher implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestDispatcher.class);

    private final ConnegHandler connegHandler;
    private final ExceptionMapper exceptionMapper;

    RestDispatcher(Consumer<RestExchange> endpoint, List<Interceptor> interceptors, ExceptionMapper exceptionMapper, MediaTypes... mimeTypes) {
        this.exceptionMapper = exceptionMapper;
        RestEntrypoint restEntrypoint = new RestEntrypoint(endpoint, interceptors, exceptionMapper);
        this.connegHandler = new ConnegHandler(restEntrypoint, exceptionMapper, mimeTypes);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            this.connegHandler.handleRequest(exchange);
        } catch (UnsupportedMediaType connex) {
            logger.error("Unsupported media type {}, possible values: {}", connex.headerValues, connex.types, connex);

            sendErrorResponse(exchange, connex);
            exchange.endExchange();

        } catch (Exception e) {
            HttpString requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestPath();
            logger.error("Exception was thrown from " + requestMethod + " " + requestPath, e);
            sendErrorResponse(exchange, e);
            exchange.endExchange();

        }
    }

    private <T extends Exception> void sendErrorResponse(HttpServerExchange exchange, T e) {
        ErrorHandler<T> errorHandler = exceptionMapper.getOrFallback(e);
        if (errorHandler == null) {
            errorHandler = exceptionMapper.getFallbackInternalError();
        }
        try {
            errorHandler.onException(e, new RestExchange(exchange));
        } catch (Exception handlingError) {
            logger.error("Exception was thrown when executing exception handler: {}, no body will be sent", errorHandler.getClass().getName(), handlingError);
        }
    }

}
