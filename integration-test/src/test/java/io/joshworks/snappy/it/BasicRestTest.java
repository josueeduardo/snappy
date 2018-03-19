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
import io.joshworks.snappy.it.util.SampleData;
import io.joshworks.snappy.it.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;

import static io.joshworks.snappy.SnappyServer.delete;
import static io.joshworks.snappy.SnappyServer.enableTracer;
import static io.joshworks.snappy.SnappyServer.get;
import static io.joshworks.snappy.SnappyServer.post;
import static io.joshworks.snappy.SnappyServer.put;
import static io.joshworks.snappy.SnappyServer.start;
import static io.joshworks.snappy.SnappyServer.stop;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by josh on 3/10/17.
 */
public class BasicRestTest {

    private static final String SERVER_URL = "http://localhost:9000";

    private static final SampleData payload = new SampleData("Yolo");

    @BeforeClass
    public static void setup() {

        enableTracer();

        get("/echo", exchange -> exchange.send(payload));
        post("/echo", exchange -> exchange.send(exchange.body().asObject(SampleData.class)));
        put("/echo", exchange -> exchange.send(exchange.body().asObject(SampleData.class)));
        delete("/echo", exchange -> exchange.send(exchange.body().asObject(SampleData.class)));
        post("/encoding", exchange -> exchange.send(Utils.toString(exchange.body().asBinary()), "txt"));

        get("/statusOnly", exchange -> exchange.status(401));

        get("/wildcard/*", exchange -> exchange.status(200));

        get("/seeOther", exchange -> exchange.seeOther(URI.create(SERVER_URL + "/echo")));
        get("/temporaryRedirect", exchange -> exchange.temporaryRedirect(URI.create(SERVER_URL + "/echo")));
        get("/seeOtherRelative", exchange -> exchange.temporaryRedirect(URI.create("/echo")));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
        Unirest.close();
    }

    @Test
    public void getRequest() {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/echo").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void postRequest() {
        HttpResponse<SampleData> response = Unirest.post(SERVER_URL + "/echo")
                .header("Content-Type", "application/json")
                .body(payload)
                .asObject(SampleData.class);

        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void putRequest() {
        HttpResponse<SampleData> response = Unirest.put(SERVER_URL + "/echo")
                .header("Content-Type", "application/json")
                .body(payload)
                .asObject(SampleData.class);

        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void deleteRequest() {
        HttpResponse<SampleData> response = Unirest.delete(SERVER_URL + "/echo")
                .header("Content-Type", "application/json")
                .body(payload)
                .asObject(SampleData.class);

        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void statusOnly() {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/statusOnly").asString();
        assertEquals(401, response.getStatus());
    }

    @Test
    public void trailingSlash() throws Exception {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/echo/").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void wildcard_exactMatch() {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/wildcard").asObject(SampleData.class);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void wildcard_withAdditionalPath() {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/wildcard/123").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void wildcard_withMultiplePathPath() {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/wildcard/123/456").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void encoding() {
        String sourceString = "'\"@こんにちは-test-123";
        byte[] sentBytes = sourceString.getBytes();

        HttpResponse<String> response = Unirest.post(SERVER_URL + "/encoding")
                .body(sentBytes)
                .asString();

        assertEquals(200, response.getStatus());
        assertEquals(sourceString, response.getBody());
    }

    @Test
    public void seeOther() {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/seeOther").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void seeOther_relativePath() {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/seeOtherRelative").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void temporaryRedirect() {
        HttpResponse<SampleData> response = Unirest.get(SERVER_URL + "/temporaryRedirect").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

}
