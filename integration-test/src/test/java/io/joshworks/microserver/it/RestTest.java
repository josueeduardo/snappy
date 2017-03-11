package io.joshworks.microserver.it;

import io.joshworks.microserver.Microserver;
import io.joshworks.microserver.it.util.SampleData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.microserver.Endpoint.path;
import static io.joshworks.microserver.client.Clients.client;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/10/17.
 */
public class RestTest {

    private static Microserver server = new Microserver();
    private static final String SERVER_URL = "http://localhost:8080";
    private static final String TEST_RESOURCE = "/test";
    private static final String RESOURCE_PATH = SERVER_URL + TEST_RESOURCE;

    private static final SampleData payload = new SampleData("Yolo");

    @BeforeClass
    public static void start() {
        path("/")
                .get(TEST_RESOURCE, (exchange) -> exchange.send(payload))
                .post(TEST_RESOURCE, (exchange) -> exchange.send(exchange.body(SampleData.class)))
                .put(TEST_RESOURCE, (exchange) -> exchange.send(exchange.body(SampleData.class)))
                .delete(TEST_RESOURCE, (exchange) -> exchange.send(exchange.body(SampleData.class)));
        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void getRequest() {
        SampleData response = client().get(RESOURCE_PATH, SampleData.class);
        assertEquals(payload.value, response.value);
    }

    @Test
    public void postRequest() {
        SampleData response = client().post(RESOURCE_PATH, payload, SampleData.class);
        assertEquals(payload.value, response.value);
    }

    @Test
    public void putRequest() {
        SampleData response = client().put(RESOURCE_PATH, payload, SampleData.class);
        assertEquals(payload.value, response.value);
    }

    @Test
    public void deleteRequest() {
        SampleData response = client().delete(RESOURCE_PATH, payload, SampleData.class);
        assertEquals(payload.value, response.value);
    }


}
