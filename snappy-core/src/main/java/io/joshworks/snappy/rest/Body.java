/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

/**
 * Created by josh on 3/12/17.
 */
public class Body {

    private final InputStream is;
    private final HeaderMap requestHeaders;
    private final MediaType negotiatedConsumeType;

    public Body(HttpServerExchange exchange) {
        this.is = exchange.getInputStream();
        ConnegHandler.NegotiatedMediaType negotiatedMediaType = exchange.getAttachment(ConnegHandler.NEGOTIATED_MEDIA_TYPE);
        this.negotiatedConsumeType = negotiatedMediaType.consumes;
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

    public <T> T asObject(Class<T> type) {
        Parser parser = Parsers.getParser(negotiatedConsumeType);

        //ref: http://web.archive.org/web/20140531042945/https://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        Scanner s = new Scanner(is, getCharset()).useDelimiter("\\A");
        String data = s.hasNext() ? s.next() : "";
        return parser.readValue(data, type);
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
