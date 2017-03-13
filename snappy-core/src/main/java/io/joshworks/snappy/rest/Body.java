package io.joshworks.snappy.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.utils.ResponseUtils;
import io.joshworks.snappy.parser.Parser;
import io.joshworks.snappy.parser.ParserUtil;
import io.joshworks.snappy.parser.Parsers;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by josh on 3/12/17.
 */
public class Body {

    private final InputStream is;
    private final HeaderMap requestHeaders;

    public Body(HttpServerExchange exchange) {
        this.is = exchange.getInputStream();
        this.requestHeaders = exchange.getRequestHeaders();
    }

    public InputStream asBinary() {
        try {
            byte[] bytes = ResponseUtils.getBytes(this.is);
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String asString() {
        byte[] rawBody;
        try {
            InputStream inputStream = is;
            if (ParserUtil.isGzipped(requestHeaders.get(Headers.CONTENT_ENCODING))) {
                inputStream = new GZIPInputStream(is);
            }
            rawBody = ResponseUtils.getBytes(inputStream);
            return new String(rawBody);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    public JsonNode asJson() {
        try {
            byte[] bytes = ResponseUtils.getBytes(this.is);
            String jsonString = new String(bytes, getCharset()).trim();
            return new JsonNode(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> asJsonMap() {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return new Gson().fromJson(asString(), type);
    }

    //TODO this the only one which requires content negotiation
    public <T> T asObject(Class<T> type) {
        return new Gson().fromJson(new InputStreamReader(is), type);
    }

    private Parser getReadParserForContentType() {
        return Parsers.find(requestHeaders.get(Headers.CONTENT_TYPE));
    }

    private Parser getReadParser(String contentType) {
        return Parsers.getParser(contentType);
    }


    private String getCharset() {
        String charset = "UTF-8";
        HeaderValues encodings = requestHeaders.get(Headers.CONTENT_ENCODING);
        if (encodings != null && encodings.isEmpty()) {
            charset = encodings.getFirst();
        }
        return charset;
    }
}
