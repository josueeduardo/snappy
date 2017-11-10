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
import io.joshworks.restclient.http.SimpleClient;
import io.joshworks.snappy.it.util.SampleData;
import io.joshworks.snappy.it.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class RestTest {

    private static final String SERVER_URL = "http://localhost:9000";
    private static final String TEST_RESOURCE = "/echo";
    private static final String RESOURCE_PATH = SERVER_URL + TEST_RESOURCE;

    private static final SampleData payload = new SampleData("Yolo");

    @BeforeClass
    public static void setup() {

        enableTracer();

        get(TEST_RESOURCE, exchange -> exchange.send(payload));
        post(TEST_RESOURCE, exchange -> exchange.send(exchange.body().asObject(SampleData.class)));
        put(TEST_RESOURCE, exchange -> exchange.send(exchange.body().asObject(SampleData.class)));
        delete(TEST_RESOURCE, exchange -> exchange.send(exchange.body().asObject(SampleData.class)));
        post("/encoding", exchange -> exchange.send(Utils.toString(exchange.body().asBinary()), "txt"));

        get("/statusOnly", exchange -> exchange.status(401));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void getRequest() throws Exception {
        HttpResponse<SampleData> response = SimpleClient.get(RESOURCE_PATH).asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void postRequest() throws Exception {
        HttpResponse<SampleData> response = SimpleClient.post(RESOURCE_PATH)
                .header("Content-Type", "application/json")
                .body(payload)
                .asObject(SampleData.class);

        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void putRequest() throws Exception {
        HttpResponse<SampleData> response = SimpleClient.put(RESOURCE_PATH)
                .header("Content-Type", "application/json")
                .body(payload)
                .asObject(SampleData.class);

        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void deleteRequest() throws Exception {
        HttpResponse<SampleData> response = SimpleClient.delete(RESOURCE_PATH)
                .header("Content-Type", "application/json")
                .body(payload)
                .asObject(SampleData.class);

        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void statusOnly() throws Exception {
        HttpResponse<String> response = SimpleClient.get(SERVER_URL + "/statusOnly").asString();
        assertEquals(401, response.getStatus());
    }

    @Test
    public void trailingSlash() throws Exception {
        HttpResponse<SampleData> response = SimpleClient.get(RESOURCE_PATH + "/").asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void encoding() {
        String sourceString = "'\"@こんにちは-test-123";
        byte[] sentBytes = sourceString.getBytes();

        HttpResponse<String> response = SimpleClient.post(SERVER_URL + "/encoding")
                .body(sentBytes)
                .asString();

        assertEquals(200, response.getStatus());
        assertEquals(sourceString, response.getBody());
    }



}
