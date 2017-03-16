//package io.joshworks.snappy.rest;
//
//import io.joshworks.snappy.handler.ConnegHandler;
//import io.joshworks.snappy.parser.Parser;
//import io.joshworks.snappy.parser.Parsers;
//import io.undertow.server.HttpServerExchange;
//import io.undertow.util.Headers;
//import io.undertow.util.HttpString;
//
///**
// * Created by josh on 3/12/17.
// */
//public class Response {
//
//    private final HttpServerExchange exchange;
//    private int status = 200;
//    private MediaType contentType = MediaType.APPLICATION_JSON_TYPE;
//
//    //TODO check the default status
//    public Response(HttpServerExchange exchange) {
//        this.exchange = exchange;
//        ConnegHandler.NegotiatedMediaType attachment = exchange.getAttachment(ConnegHandler.NEGOTIATED_MEDIA_TYPE);
//        if (attachment != null) { //should not happen
//            MediaType negotiated = attachment.produces;
//            //If client accepts anything, json will be used
//            if (negotiated != null && !negotiated.equals(MediaType.WILDCARD_TYPE)) {
//                this.contentType = negotiated;
//            }
//            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType.toString());
//        }
//    }
//
//    public Response header(String name, String value) {
//        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
//        return this;
//    }
//
//    public Response header(String name, long value) {
//        exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
//        return this;
//    }
//
//    public Response contentType(MediaType mediaType) {
//        this.contentType = mediaType;
//        return this;
//    }
//
//    public Response contentType(String mediaType) {
//        return this.contentType(MediaType.valueOf(mediaType));
//    }
//
//    public Response status(int status) {
//        this.status = status;
//        return this;
//    }
//
//    public void send(Object response) {
//        this.response(response);
//    }
//
//    public void send(Object response, String mediaType) {
//        send(response, MediaType.valueOf(mediaType));
//    }
//
//    public void send(Object response, MediaType mediaType) {
//        contentType(mediaType);
//        this.response(response);
//    }
//
//    private void response(Object response) {
//        try {
//            Parser responseParser = Parsers.getParser(contentType);
//            if (responseParser == null) {
//                //TODO return as an 500 here instead
//                throw new RuntimeException("Could not find Parser for type " + contentType.toString());
//            }
//            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType.toString());
//            exchange.setStatusCode(status);
//            exchange.getResponseSender().send(responseParser.writeValue(response));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//}
