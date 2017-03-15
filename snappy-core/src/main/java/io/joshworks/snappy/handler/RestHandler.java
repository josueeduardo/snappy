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
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Josh Gontijo on 3/5/17.
 * <p>
 * This should be the higher level Rest class, ideally this is wrapped in the Error handler which will return
 * handler exceptions thrown by the endpoints
 */
public class RestHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestHandler.class);

    public static final AttachmentKey<NegotiatedMediaType> NEGOTIATED_MEDIA_TYPE = AttachmentKey.create(NegotiatedMediaType.class);

    private final Consumer<RestExchange> endpoint;
    private List<Interceptor> interceptors = new ArrayList<>();

    private MediaTypes consumes;
    private MediaTypes produces;

    RestHandler(Consumer<RestExchange> endpoint, MediaTypes... mimeTypes) {
        this.endpoint = endpoint;
        initTypes(mimeTypes);
        consumes = consumes == null ? MediaTypes.DEFAULT_CONSUMES : consumes;
        produces = produces == null ? MediaTypes.DEFAULT_PRODUCES : produces;

    }

    //TODO improve this ugly code
    private void initTypes(MediaTypes... mimeTypes) {
        for (MediaTypes type : mimeTypes) {
            if (MediaTypes.Context.CONSUMES.equals(type.getContext())) {
                if (consumes != null) {
                    consumes.addAll(type);
                } else {
                    consumes = type;
                }
            } else {
                if (produces != null) {
                    produces.addAll(type);
                } else {
                    produces = type;
                }
            }
        }
    }

    private MediaType matchProducesMime(HttpServerExchange exchange) {
        HeaderValues acceptHeader = exchange.getRequestHeaders().get(Headers.ACCEPT);
        boolean hasAcceptHeader = acceptHeader != null && !acceptHeader.isEmpty();
        if (hasAcceptHeader) {
            return produces.match(acceptHeader);
        }
        //if no Accept header is specified, the first specified by user OR default is used, PLAIN otherwise
        Iterator<MediaType> iterator = produces.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return MediaType.TEXT_PLAIN_TYPE;
    }

    private MediaType matchConsumesMime(HttpServerExchange exchange) {
        HeaderValues bodyContentType = exchange.getRequestHeaders().get(Headers.CONTENT_TYPE);
        boolean hasContentType = bodyContentType != null && !bodyContentType.isEmpty();
        if (hasContentType) {
            return consumes.match(bodyContentType);
        }
        //If no content type is specified, text/plain is used
        return MediaType.TEXT_PLAIN_TYPE;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        contentNegotiation(httpServerExchange);//Must come before
        RestExchange restExchange = new RestExchange(httpServerExchange);

        for (Interceptor interceptor : interceptors) {
            if (!httpServerExchange.isResponseComplete()) {
                interceptor.handleRequest(restExchange);
            }
        }
        if (!httpServerExchange.isResponseComplete()) {
            handle(restExchange);
        }
    }

    private void handle(RestExchange exchange) {
        try {
            endpoint.accept(exchange);
        } catch (Exception e) {
            HttpString requestMethod = exchange.httpServerExchange.getRequestMethod();
            String requestPath = exchange.httpServerExchange.getResolvedPath();
            logger.error("Exception was thrown from " + requestMethod + " " + requestPath, e);

            exchange.httpServerExchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            sendErrorResponse(exchange.httpServerExchange, e.getMessage());
        }
    }

    private void contentNegotiation(HttpServerExchange httpServerExchange) {
        MediaType consumesType = matchConsumesMime(httpServerExchange);
        if (consumesType == null) {
            invalidMediaType(httpServerExchange, Headers.CONTENT_TYPE);
            return;
        }

        MediaType producesType = matchProducesMime(httpServerExchange);
        if (producesType == null) {
            invalidMediaType(httpServerExchange, Headers.ACCEPT);
            return;
        }

        httpServerExchange.putAttachment(NEGOTIATED_MEDIA_TYPE, new NegotiatedMediaType(consumesType, producesType));
    }

    private void invalidMediaType(HttpServerExchange exchange, HttpString headerName) {
        HeaderValues headerValues = exchange.getRequestHeaders().get(headerName);
        String values = headerValues != null ? headerValues.toString() : "";
        logger.warn("Unsupported media type {}: {}", headerValues, values);

        exchange.setStatusCode(StatusCodes.UNSUPPORTED_MEDIA_TYPE);

        String acceptedTypes = produces.stream().map(MediaType::toString).collect(Collectors.joining(", "));
        sendErrorResponse(exchange, "Unsupported media type, acceptable types [" + acceptedTypes + "]");
        exchange.endExchange();
    }


    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public static class NegotiatedMediaType {
        public final MediaType consumes;
        public final MediaType produces;

        public NegotiatedMediaType(MediaType consumes, MediaType produces) {
            this.consumes = consumes;
            this.produces = produces;
        }
    }

    //TODO add dev environment toggle features
    public void sendErrorResponse(HttpServerExchange exchange, String message) {
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
