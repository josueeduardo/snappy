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

package io.joshworks.snappy.http;

import io.joshworks.snappy.Exchange;
import io.undertow.server.HttpServerExchange;

/**
 * Created by josh on 3/5/17.
 */
public class HttpExchange extends Exchange {

    private Body body;

    public HttpExchange(HttpServerExchange exchange) {
        super(exchange);
        setNegotiatedContentType();
    }

    //If client accepts anything, json will be used
    private void setNegotiatedContentType() {
        ConnegHandler.NegotiatedMediaType negotiatedMediaType = exchange.getAttachment(ConnegHandler.NEGOTIATED_MEDIA_TYPE);
        MediaType negotiated = negotiatedMediaType == null ? responseContentType : negotiatedMediaType.produces;
        setResponseMediaType(negotiated);
    }

    public Body body() {
        if (body == null) {
            this.body = new Body(exchange);
        }
        return body;
    }

}
