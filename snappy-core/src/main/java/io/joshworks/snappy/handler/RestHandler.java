package io.joshworks.snappy.handler;

import io.joshworks.snappy.parser.MediaTypes;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.RestEndpoint;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by josh on 3/5/17.
 */
public class RestHandler implements HttpHandler {

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

    private boolean matchMimeType(HttpServerExchange exchange) {
        HeaderValues acceptHeader = exchange.getRequestHeaders().get(Headers.ACCEPT);
        return acceptHeader == null || acceptHeader.isEmpty() || !consumes.match(acceptHeader).isEmpty();
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        RestExchange restExchange = new RestExchange(httpServerExchange);

        if (!matchMimeType(httpServerExchange)) {
            httpServerExchange.setStatusCode(415);
            httpServerExchange.endExchange();
        } else {

            for (Interceptor interceptor : interceptors) {
                interceptor.handleRequest(restExchange);
            }
            if (!httpServerExchange.isResponseComplete()) {
                endpoint.handle(restExchange);
            }
        }
    }

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }


}
