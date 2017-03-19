package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.joshworks.snappy.SnappyServer.LOGGER_NAME;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class ConnegHandler implements HttpHandler {

    public static final AttachmentKey<NegotiatedMediaType> NEGOTIATED_MEDIA_TYPE = AttachmentKey.create(NegotiatedMediaType.class);
    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
    private final HttpHandler next;
    private final ExceptionMapper exceptionMapper;
    private MediaTypes consumes;
    private MediaTypes produces;

    public ConnegHandler(HttpHandler next, ExceptionMapper exceptionMapper, MediaTypes... mimeTypes) {
        this.exceptionMapper = exceptionMapper;
        initTypes(mimeTypes);
        consumes = consumes == null ? MediaTypes.DEFAULT_CONSUMES : consumes;
        produces = produces == null ? MediaTypes.DEFAULT_PRODUCES : produces;
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        MediaType consumesType = matchConsumesMime(exchange);
        if (consumesType == null) {
            invalidMediaType(exchange, Headers.CONTENT_TYPE);
            return;
        }

        MediaType producesType = matchProducesMime(exchange);
        if (producesType == null) {
            invalidMediaType(exchange, Headers.ACCEPT);
            return;
        }

        exchange.putAttachment(NEGOTIATED_MEDIA_TYPE, new NegotiatedMediaType(consumesType, producesType));
        next.handleRequest(exchange);
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

    private void invalidMediaType(HttpServerExchange exchange, HttpString headerName) throws UnsupportedMediaType {
        HeaderValues headerValues = exchange.getRequestHeaders().get(headerName);
        exchange.setStatusCode(StatusCodes.UNSUPPORTED_MEDIA_TYPE);
        throw UnsupportedMediaType.unsuportedMediaType(headerValues, produces);
    }

    private MediaType matchProducesMime(HttpServerExchange exchange) {
        HeaderValues acceptHeader = exchange.getRequestHeaders().get(Headers.ACCEPT);
        boolean hasAcceptHeader = acceptHeader != null && !acceptHeader.isEmpty();
        if (hasAcceptHeader) {
            return produces.match(acceptHeader);
        }
        //if no Accept header is specified, the first specified by user OR default is used
        return produces.getDefaultType();
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

    public static class NegotiatedMediaType {
        public final MediaType consumes;
        public final MediaType produces;

        NegotiatedMediaType(MediaType consumes, MediaType produces) {
            this.consumes = consumes;
            this.produces = produces;
        }
    }
}
