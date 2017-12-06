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

package io.joshworks.snappy.it;

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import io.joshworks.snappy.http.MediaType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static io.joshworks.snappy.parser.MediaTypes.consumes;
import static io.joshworks.snappy.parser.MediaTypes.produces;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by josh on 3/12/17.
 */
public class ConnegTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";

    @BeforeClass
    public static void setup() {
        basePath("/v1");

        get("/json", exchange -> {
        });

        get("/default", exchange -> {
        });

        get("/xml", exchange -> {
        }, consumes("application/xml"));

        get("/simple-mime", exchange -> {
        }, consumes("json"));

        //produces
        get("/produces-json", exchange -> {
        }, produces("application/json"));

        get("/produces-text", exchange -> {
        }, produces("text/plain"));

        get("/overridden", exchange -> {
            exchange.send("{}", MediaType.APPLICATION_JSON_TYPE);
        }, produces("txt"));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    // ----- SERVER CONSUMES -----
    @Test
    public void supportedMediaType() throws Exception {
        int responseStatus = Unirest.get("http://localhost:9000/v1/json").header(ACCEPT, "application/json").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void acceptsAll() throws Exception {
        int responseStatus = Unirest.get("http://localhost:9000/v1/json").header(ACCEPT, "*/*").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void supportedMediaTypeWithCharset() throws Exception {
        int responseStatus = Unirest.get("http://localhost:9000/v1/json").header(ACCEPT, "application/json; charset=UTF-8").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void unsupportedMediaType_withDefaults() throws Exception {
        int responseStatus = Unirest.get("http://localhost:9000/v1/json").header(ACCEPT, "application/xml").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void supportedMediaType_with_provided_value() throws Exception {
        int responseStatus = Unirest.get("http://localhost:9000/v1/xml").header(ACCEPT, "application/xml").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void simpleMime() throws Exception {
        HttpResponse<String> response = Unirest.get("http://localhost:9000/v1/simple-mime").header(ACCEPT, "application/json").asString();
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get(CONTENT_TYPE));
        assertEquals("application/json", response.getHeaders().get(CONTENT_TYPE).get(0));
    }

    @Test
    public void supportedMediaType_prefer_json() throws Exception {
        HttpResponse<String> stringHttpResponse = Unirest.get("http://localhost:9000/v1/xml")
                .header(ACCEPT, "application/json")
                .header(ACCEPT, "text/plain")
                .asString();
        assertHeaderEquals("application/json", stringHttpResponse);
    }

    @Test
    public void supportedMediaType_prefer_plain() throws Exception {
        HttpResponse<String> stringHttpResponse = Unirest.get("http://localhost:9000/v1/json")
                .header(ACCEPT, "text/plain")
                .header(ACCEPT, "application/json")
                .asString();
        assertEquals(200, stringHttpResponse.getStatus());
        assertHeaderEquals("text/plain", stringHttpResponse);
    }

    @Test
    public void validRequestPayloadMime() throws Exception {
        int responseStatus = Unirest.get("http://localhost:9000/v1/xml").header(CONTENT_TYPE, "application/xml").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void invalidRequestPayloadMime() throws Exception {
        int responseStatus = Unirest.get("http://localhost:9000/v1/xml").header(CONTENT_TYPE, "application/json").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void validRequestPayloadMime_with_consumes_sameType() throws Exception {
        HttpResponse<String> response = Unirest.get("http://localhost:9000/v1/json")
                .header(CONTENT_TYPE, "application/json")
                .header(ACCEPT, "application/json")
                .asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }


    // ----- SERVER PRODUCES -----
    @Test //this tests the order in which the produces are registered in the MediaTypes
    public void noAcceptSpecified_default_json() throws Exception {
        HttpResponse<String> response = Unirest.get("http://localhost:9000/v1/json").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }

    @Test
    public void producesTextPlain() throws Exception {
        HttpResponse<String> response = Unirest.get("http://localhost:9000/v1/produces-text").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("text/plain", response);
    }

    @Test
    public void producesJson() throws Exception {
        HttpResponse<String> response = Unirest.get("http://localhost:9000/v1/produces-json").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }

    @Test
    public void contentTypeOverridden() throws Exception {
        HttpResponse<String> response = Unirest.get("http://localhost:9000/v1/overridden").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }

    private void assertHeaderEquals(String expected, HttpResponse response) {
        assertNotNull(response.getHeaders().get(CONTENT_TYPE));
        assertEquals(1, response.getHeaders().get(CONTENT_TYPE).size());
        assertEquals(expected, response.getHeaders().get(CONTENT_TYPE).get(0));
    }

}
