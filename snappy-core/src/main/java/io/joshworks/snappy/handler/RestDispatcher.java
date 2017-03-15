package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.parser.Parsers;
import io.joshworks.snappy.rest.ExceptionResponse;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.MediaType;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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

    RestDispatcher(Consumer<RestExchange> endpoint, List<Interceptor> interceptors, MediaTypes... mimeTypes) {
        RestEntrypoint restEntrypoint = new RestEntrypoint(endpoint, interceptors);
        this.connegHandler = new ConnegHandler(restEntrypoint, mimeTypes);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        try {
            this.connegHandler.handleRequest(exchange);
        } catch (ConnegException connex) {

            logger.error("Unsupported media type {}, possible values: {}", connex.headerValues, connex.types);
            sendErrorResponse(exchange, connex.getMessage());

        } catch (Exception e) {
            HttpString requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getResolvedPath();
            logger.error("Exception was thrown from " + requestMethod + " " + requestPath, e);

            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            sendErrorResponse(exchange, e.getMessage());
        }
    }

    //TODO add dev environment toggle features
    private void sendErrorResponse(HttpServerExchange exchange, String message) {
        int responseStatus = exchange.getStatusCode();
        if (!exchange.getResponseHeaders().contains(Headers.CONTENT_TYPE)) {
            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        ExceptionResponse exceptionResponse = new ExceptionResponse(responseStatus, message);

        HeaderValues contentType = exchange.getResponseHeaders().get(Headers.CONTENT_TYPE);
        try {
            Parser parser = Parsers.find(contentType);
            String responseData = parser.writeValue(exceptionResponse);
            exchange.getResponseSender().send(responseData);
        } catch (Exception e1) {
            logger.warn("Could not find a parser or parse data for media type(s) {} when sending exception message", Arrays.toString(contentType.toArray()));
        }
    }

}
