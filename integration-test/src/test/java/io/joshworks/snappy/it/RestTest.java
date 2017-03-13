package io.joshworks.snappy.it;

import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.it.util.SampleData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/10/17.
 */
public class RestTest {

    private static SnappyServer server = new SnappyServer();
    private static final String SERVER_URL = "http://localhost:8080";
    private static final String TEST_RESOURCE = "/test";
    private static final String RESOURCE_PATH = SERVER_URL + TEST_RESOURCE;

    private static final SampleData payload = new SampleData("Yolo");

    @BeforeClass
    public static void start() {
        server
                .get(TEST_RESOURCE, (exchange) -> exchange.response().send(payload))
                .post(TEST_RESOURCE, (exchange) -> exchange.response().send(exchange.body().asObject(SampleData.class)))
                .put(TEST_RESOURCE, (exchange) -> exchange.response().send(exchange.body().asObject(SampleData.class)))
                .delete(TEST_RESOURCE, (exchange) -> exchange.response().send(exchange.body().asObject(SampleData.class)));

        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void getRequest() throws Exception {
        SampleData body = RestClient.get(RESOURCE_PATH).asObject(SampleData.class).getBody();
        assertEquals(payload.value, body.value);
    }

    @Test
    public void postRequest() throws Exception {
        SampleData response = RestClient.post(RESOURCE_PATH).body(payload).asObject(SampleData.class).getBody();
        assertEquals(payload.value, response.value);
    }

    @Test
    public void putRequest() throws Exception {
        SampleData response = RestClient.put(RESOURCE_PATH).body(payload).asObject(SampleData.class).getBody();
        assertEquals(payload.value, response.value);
    }

    @Test
    public void deleteRequest() throws Exception {
        SampleData response = RestClient.delete(RESOURCE_PATH).body(payload).asObject(SampleData.class).getBody();
        assertEquals(payload.value, response.value);
    }


}
