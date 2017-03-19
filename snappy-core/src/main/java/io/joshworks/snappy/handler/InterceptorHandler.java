package io.joshworks.snappy.handler;

import io.joshworks.snappy.Exchange;
import io.joshworks.snappy.rest.ErrorHandler;
import io.joshworks.snappy.rest.ExceptionMapper;
import io.joshworks.snappy.rest.Interceptor;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 3/19/17.
 */
public class InterceptorHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private HttpHandler next = ResponseCodeHandler.HANDLE_404;

    private final List<Interceptor> interceptors;
    private ExceptionMapper exceptionMapper = new ExceptionMapper();

    public InterceptorHandler(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public InterceptorHandler(List<Interceptor> interceptors, ExceptionMapper exceptionMapper) {
        this.interceptors = interceptors;
        this.exceptionMapper = exceptionMapper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String url = exchange.getRequestPath();

        boolean proceed = intercept(Interceptor.Type.BEFORE, exchange, url);
        if (!proceed) {
            return;
        }

        exchange.addExchangeCompleteListener((exchange1, nextListener) -> {
            boolean afterProceed = intercept(Interceptor.Type.AFTER, exchange, url);
            if (afterProceed) {
                nextListener.proceed();
            }
        });

        next.handleRequest(exchange);

    }

    private boolean intercept(Interceptor.Type type, HttpServerExchange exchange, String url) {
        Exchange requestExchange = new Exchange(exchange);
        List<Interceptor> matches = interceptors.stream().filter(i -> i.match(type, url)).collect(Collectors.toList());
        for (Interceptor interceptor : matches) {
            try {
                interceptor.intercept(requestExchange);
            } catch (Exception ex) {
                logger.error("Error handling {} interceptor for url {}, next interceptor will not proceed", type.name(), url);
                if (Interceptor.Type.BEFORE.equals(type)) {
                    ErrorHandler<Exception> orFallback = exceptionMapper.getOrFallback(ex);
                    orFallback.onException(ex, requestExchange);
                }
                if (!exchange.isComplete()) {
                    exchange.endExchange();
                }
                return false;
            }
        }
        return true;
    }

    public void setNext(HttpHandler next) {
        this.next = next;
    }
}