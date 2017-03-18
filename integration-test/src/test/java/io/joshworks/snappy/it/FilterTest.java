package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by josh on 3/10/17.
 */
public class FilterTest {

    private static final String SERVER_URL = "http://localhost:8080";

    private static boolean filterAfterCalled = false;

    @BeforeClass
    public static void setup() {
        get("/a", (exchange) -> exchange.send("A", "txt"));
        get("/b", (exchange) -> exchange.send("B", "txt"));
        get("/b/c", (exchange) -> exchange.send("B", "txt"));
        get("/a/c", (exchange) -> exchange.send("AC", "txt"));
        get("/a/c/d", (exchange) -> exchange.send("ACD", "txt"));

        before("/a/*", (exchange) -> exchange.header("FILTER-1", "OK"));
        before("/a/c/*", (exchange) -> exchange.header("FILTER-2", "OK"));
        before("/b", (exchange) -> exchange.header("EXACT-FILTER", "OK"));

        after("/b", (exchange) -> filterAfterCalled = true);

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void noFilter() throws Exception {
        HttpResponse<String> response = RestClient.get(SERVER_URL + "/a").asString();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void singleFilter() throws Exception {
        HttpResponse<String> response = RestClient.get(SERVER_URL + "/a/c").asString();
        assertNotNull(response.getHeaders().get("FILTER-1"));
        assertEquals(1, response.getHeaders().get("FILTER-1").size());

        assertNull(response.getHeaders().get("FILTER-2"));
        assertNull(response.getHeaders().get("EXACT-FILTER"));
    }

    @Test
    public void multiple() throws Exception {
        HttpResponse<String> response = RestClient.get(SERVER_URL + "/a/c/d").asString();
        assertNotNull(response.getHeaders().get("FILTER-1"));
        assertEquals(1, response.getHeaders().get("FILTER-1").size());

        assertNotNull(response.getHeaders().get("FILTER-2"));
        assertEquals(1, response.getHeaders().get("FILTER-2").size());

        assertNull(response.getHeaders().get("EXACT-FILTER"));
    }

    @Test
    public void exact() throws Exception {
        HttpResponse<String> response = RestClient.get(SERVER_URL + "/b").asString();
        assertNotNull(response.getHeaders().get("EXACT-FILTER"));
        assertEquals(1, response.getHeaders().get("EXACT-FILTER").size());

        assertNull(response.getHeaders().get("FILTER-1"));
        assertNull(response.getHeaders().get("FILTER-2"));
    }

    @Test
    public void exact_no_match() throws Exception {
        HttpResponse<String> response = RestClient.get(SERVER_URL + "/b/c").asString();
        assertNull(response.getHeaders().get("EXACT-FILTER"));
        assertNull(response.getHeaders().get("FILTER-1"));
        assertNull(response.getHeaders().get("FILTER-2"));
    }

    @Test
    public void filterAfter() throws Exception {
        HttpResponse<String> response = RestClient.get(SERVER_URL + "/b").asString();
        assertNotNull(response.getHeaders().get("EXACT-FILTER"));
        assertEquals(1, response.getHeaders().get("EXACT-FILTER").size());
        assertTrue(filterAfterCalled); //after filters gets called after the response was submitted

        assertNull(response.getHeaders().get("FILTER-1"));
        assertNull(response.getHeaders().get("FILTER-2"));
    }


}
