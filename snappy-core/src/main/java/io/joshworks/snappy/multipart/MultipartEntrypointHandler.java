package io.joshworks.snappy.multipart;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.function.Consumer;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class MultipartEntrypointHandler implements HttpHandler {

    private Consumer<MultipartExchange> endpoint;

    public MultipartEntrypointHandler(Consumer<MultipartExchange> endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        MultipartExchange multipartExchange = new MultipartExchange(exchange);
        endpoint.accept(multipartExchange);
    }
}
