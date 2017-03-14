package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.MediaType;
import io.joshworks.snappy.rest.RestEndpoint;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by josh on 3/5/17.
 */
public class RestHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestHandler.class);

    public static final AttachmentKey<NegotiatedMediaType> NEGOTIATED_MEDIA_TYPE = AttachmentKey.create(NegotiatedMediaType.class);

    private final RestEndpoint endpoint;
    private List<Interceptor> interceptors = new ArrayList<>();

    private MediaTypes consumes;
    private MediaTypes produces;

    RestHandler(RestEndpoint endpoint, MediaTypes... mimeTypes) {
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
            endpoint.handle(restExchange);
        }
    }

    private void contentNegotiation(HttpServerExchange httpServerExchange) {
        MediaType consumesType = matchConsumesMime(httpServerExchange);
        if (consumesType == null) {
            HeaderValues bodyContentType = httpServerExchange.getRequestHeaders().get(Headers.CONTENT_TYPE);
            logger.warn("Server cannot read body for Content-Type header: " + bodyContentType.toString());
            httpServerExchange.setStatusCode(StatusCodes.UNSUPPORTED_MEDIA_TYPE);
            httpServerExchange.endExchange();
            return;
        }

        MediaType producesType = matchProducesMime(httpServerExchange);
        if (producesType == null) {
            HeaderValues bodyContentType = httpServerExchange.getRequestHeaders().get(Headers.ACCEPT);
            String accepts = bodyContentType != null ? bodyContentType.toString() : "";
            logger.warn("Server cannot send body for Accept header: " + accepts);
            //TODO return a nice json with the allowed typed ?
            httpServerExchange.setStatusCode(StatusCodes.UNSUPPORTED_MEDIA_TYPE);
            httpServerExchange.endExchange();
            return;
        }

//        httpServerExchange.getResponseHeaders().add(Headers.CONTENT_TYPE, producesType.toString());
        httpServerExchange.putAttachment(NEGOTIATED_MEDIA_TYPE, new NegotiatedMediaType(consumesType, producesType));

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


}
