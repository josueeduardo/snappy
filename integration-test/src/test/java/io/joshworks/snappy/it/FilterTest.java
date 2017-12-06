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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Created by josh on 3/10/17.
 */
public class FilterTest {

    private static final String SERVER_URL = "http://localhost:9000";

    private static CountDownLatch filterAfterCalled;
    private static CountDownLatch filterAfterAllCalled;

    @BeforeClass
    public static void setup() {
        get("/a", exchange -> exchange.send("A", "txt"));
        get("/b", exchange -> exchange.send("B", "txt"));
        get("/b/c", exchange -> exchange.send("B", "txt"));
        get("/a/c", exchange -> exchange.send("AC", "txt"));
        get("/a/c/d", exchange -> exchange.send("ACD", "txt"));

        beforeAll("/b", exchange -> exchange.header("FILTER-BEFORE-ALL-B", "OK"));
        beforeAll("/*", exchange -> exchange.header("FILTER-BEFORE-ALL", "OK"));

        afterAll("/*", exchange -> filterAfterAllCalled.countDown());


        before("/a/*", exchange -> exchange.header("FILTER-1", "OK"));
        before("/a/c/*", exchange -> exchange.header("FILTER-2", "OK"));
        before("/b", exchange -> exchange.header("EXACT-FILTER", "OK"));

        after("/b", exchange -> filterAfterCalled.countDown());

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Before
    public void reset() {
        filterAfterCalled = new CountDownLatch(1);
        filterAfterAllCalled = new CountDownLatch(1);
    }

    @Test //only beforeAll will be applied anyway
    public void noFilter() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/a").asString();
        assertEquals(200, response.getStatus());
        assertNull(response.getHeaders().get("FILTER-1"));
        assertNull(response.getHeaders().get("EXACT-FILTER"));
        assertNull(response.getHeaders().get("FILTER-BEFORE-ALL-A"));
        assertNull(response.getHeaders().get("FILTER-AFTER-ALL"));
    }

    @Test
    public void singleFilter() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/a/c").asString();
        assertNotNull(response.getHeaders().get("FILTER-1"));
        assertEquals(1, response.getHeaders().get("FILTER-1").size());

        assertNull(response.getHeaders().get("FILTER-2"));
        assertNull(response.getHeaders().get("EXACT-FILTER"));
    }

    @Test
    public void multiple() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/a/c/d").asString();
        assertNotNull(response.getHeaders().get("FILTER-1"));
        assertEquals(1, response.getHeaders().get("FILTER-1").size());

        assertNotNull(response.getHeaders().get("FILTER-2"));
        assertEquals(1, response.getHeaders().get("FILTER-2").size());

        assertNull(response.getHeaders().get("EXACT-FILTER"));
    }

    @Test
    public void exact() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/b").asString();
        assertNotNull(response.getHeaders().get("EXACT-FILTER"));
        assertEquals(1, response.getHeaders().get("EXACT-FILTER").size());

        assertNull(response.getHeaders().get("FILTER-1"));
        assertNull(response.getHeaders().get("FILTER-2"));
    }

    @Test
    public void exact_no_match() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/b/c").asString();
        assertNull(response.getHeaders().get("EXACT-FILTER"));
        assertNull(response.getHeaders().get("FILTER-1"));
        assertNull(response.getHeaders().get("FILTER-2"));
    }

    @Test
    public void filterAfter() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/b").asString();
        assertNotNull(response.getHeaders().get("EXACT-FILTER"));
        assertEquals(1, response.getHeaders().get("EXACT-FILTER").size());
        assertNull(response.getHeaders().get("FILTER-1"));
        assertNull(response.getHeaders().get("FILTER-2"));

        if (!filterAfterCalled.await(10, TimeUnit.SECONDS)) {
            fail("Filter after wasn't called");
        }

    }

    @Test
    public void filter_exact_AllBefore() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/b").asString();
        assertNotNull(response.getHeaders().get("FILTER-BEFORE-ALL-B"));
        if (!filterAfterAllCalled.await(10, TimeUnit.SECONDS)) {
            fail("Filter after all wasn't called");
        }
    }

    @Test
    public void filter_wildcard_AllBefore() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/a").asString();
        assertNotNull(response.getHeaders().get("FILTER-BEFORE-ALL"));
    }

    @Test
    public void filter_wildcard_AllBefore_nonexistentPath() throws Exception {
        HttpResponse<String> response = Unirest.get(SERVER_URL + "/nonexistentPath").asString();
        assertEquals(404, response.getStatus());
        assertNotNull(response.getHeaders().get("FILTER-BEFORE-ALL"));
    }


}
