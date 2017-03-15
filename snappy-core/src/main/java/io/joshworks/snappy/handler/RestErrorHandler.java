//package io.joshworks.snappy.handler;
//
//import io.undertow.server.HttpHandler;
//import io.undertow.server.HttpServerExchange;
//import io.undertow.util.HttpString;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Created by Josh Gontijo on 3/15/17.
// *
// * This class is intended to be a wrapper around RestHandler only.
// * This class does not handles content negotiation / http errors, since it faster
// * to send a response without raising an exception
// *
// */
//public class RestErrorHandler implements HttpHandler {
//
//    private static final Logger logger = LoggerFactory.getLogger(RestErrorHandler.class);
//
//    private final RestHandler next;
//
//    public RestErrorHandler(RestHandler next) {
//        this.next = next;
//    }
//
//    @Override
//    public void handleRequest(HttpServerExchange exchange) throws Exception {
//        try {
//            this.next.handleRequest(exchange);
//        } catch (Exception e) {
//            HttpString requestMethod = exchange.getRequestMethod();
//            String requestPath = exchange.getResolvedPath();
//            logger.error("Exception was thrown from " + requestMethod + " " + requestPath, e);
//
//            HandlerUtil.sendErrorResponse(exchange, e.getMessage());
//        }
//    }
//
//
//}
