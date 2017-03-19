package io.joshworks.snappy.handler;

import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Interceptor;
import io.joshworks.snappy.rest.RestExchange;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.joshworks.snappy.SnappyServer.LOGGER_NAME;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class RestEntrypoint implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private final Consumer<RestExchange> endpoint;
    private final List<Interceptor> interceptors;
    private final ExceptionMapper exceptionMapper;

    public RestEntrypoint(Consumer<RestExchange> endpoint, List<Interceptor> interceptors, ExceptionMapper exceptionMapper) {
        this.endpoint = endpoint;
        this.interceptors = interceptors;
        this.exceptionMapper = exceptionMapper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        RestExchange restExchange = new RestExchange(exchange);
        try {
            String url = exchange.getRequestPath();

            intercept(Interceptor.Type.BEFORE, restExchange, url);
            if (!exchange.isResponseComplete()) {
                endpoint.accept(restExchange);
            }
            intercept(Interceptor.Type.AFTER, restExchange, url);

        } catch (Exception e) {
            ErrorHandler errorHandler = exceptionMapper.getOrFallback(e);
            if (errorHandler == null) {
                logger.error("No event handler mapped for exception " + e.getClass().getName());
                errorHandler = exceptionMapper.getFallbackInternalError();
            }
            errorHandler.onException(e, restExchange);

        }
    }

    private void intercept(Interceptor.Type type, RestExchange restExchange, String url) throws Exception {
        List<Interceptor> matches = interceptors.stream().filter(i -> i.match(type, url)).collect(Collectors.toList());
        for (Interceptor interceptor : matches) {
            interceptor.intercept(restExchange);
        }
    }

}
