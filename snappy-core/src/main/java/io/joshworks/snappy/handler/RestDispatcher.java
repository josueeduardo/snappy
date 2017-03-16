package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.parser.Parsers;
import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.ExceptionResponse;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.MediaType;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
        RestEntrypoint restEntrypoint = new RestEntrypoint(endpoint, interceptors);
        this.connegHandler = new ConnegHandler(restEntrypoint, mimeTypes);
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
        ErrorHandler errorHandler = exceptionMapper.getOrFallback(e.getClass());
        if (errorHandler == null) {
            errorHandler = exceptionMapper.getFallbackInternalError();
        }
        ExceptionResponse response = tryHandleException(e, errorHandler);
        if (response != null) {
            Set<MediaType> contentTypes = getErrorMediaType(exchange, response);
            String responseContent = parseError(exchange, contentTypes, response);
            exchange.setStatusCode(response.getStatus());
            exchange.getResponseSender().send(responseContent);
        }
    }

    private <T extends Exception> ExceptionResponse tryHandleException(T e, ErrorHandler<T> handler) {
        try {
            return handler.onException(e);
        } catch (Exception handlingError) {
            logger.error("Exception was thrown when executing exception handler: {}, no body will be sent", handler.getClass().getName(), handlingError);
            return null;
        }
    }

    private String parseError(HttpServerExchange exchange, Set<MediaType> contentTypes, ExceptionResponse exceptionResponse) {
        String responseData = null;
        try {
            if (exceptionResponse != null) {
                Parser parser = Parsers.findByType(contentTypes);
                //Sets the found parser content type as the response type, (if no other type was set)
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, parser.mediaType().toString());
                responseData = parser.writeValue(exceptionResponse.getBody());
            }
        } catch (Exception e1) {
            logger.error("Could not find a parser or parse data for media type(s) {} when sending exception message", Arrays.toString(contentTypes.toArray()), e1);
        }
        return responseData;
    }

    private Set<MediaType> getErrorMediaType(HttpServerExchange exchange, ExceptionResponse response) {
        Set<MediaType> mediaTypes = new HashSet<>();
        HeaderValues fromHeader = exchange.getResponseHeaders().get(Headers.CONTENT_TYPE);
        MediaType mediaType = response != null ? response.getMediaType() : null;
        if (mediaType != null) {
            mediaTypes.add(mediaType);
        }
        if (fromHeader != null) {
            fromHeader.forEach(MediaType::valueOf);
        }
        mediaTypes.add(MediaType.APPLICATION_JSON_TYPE); //uses json instead plain text
        return mediaTypes;
    }

}
