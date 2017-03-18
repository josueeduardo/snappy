package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.it.util.SampleData;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by josh on 3/10/17.
 */
public class RestTest {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final String TEST_RESOURCE = "/echo";
    private static final String RESOURCE_PATH = SERVER_URL + TEST_RESOURCE;

    private static final SampleData payload = new SampleData("Yolo");

    @BeforeClass
    public static void setup() {

        get(TEST_RESOURCE, (exchange) -> exchange.send(payload));
        post(TEST_RESOURCE, (exchange) -> exchange.send(exchange.body().asObject(SampleData.class)));
        put(TEST_RESOURCE, (exchange) -> exchange.send(exchange.body().asObject(SampleData.class)));
        delete(TEST_RESOURCE, (exchange) -> exchange.send(exchange.body().asObject(SampleData.class)));

        get("/statusOnly", (exchange) -> exchange.status(401));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void getRequest() throws Exception {
        HttpResponse<SampleData> response = RestClient.get(RESOURCE_PATH).asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void postRequest() throws Exception {
        HttpResponse<SampleData> response = RestClient.post(RESOURCE_PATH).body(payload).asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void putRequest() throws Exception {
        HttpResponse<SampleData> response = RestClient.put(RESOURCE_PATH).body(payload).asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void deleteRequest() throws Exception {
        HttpResponse<SampleData> response = RestClient.delete(RESOURCE_PATH).body(payload).asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }

    @Test
    public void statusOnly() throws Exception {
        HttpResponse<String> response = RestClient.get(SERVER_URL + "/statusOnly").asString();
        assertEquals(401, response.getStatus());
    }

    @Test
    public void traillingSlash() throws Exception {
        HttpResponse<SampleData> response = RestClient.get(RESOURCE_PATH).asObject(SampleData.class);
        assertEquals(200, response.getStatus());
        SampleData responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(payload.value, responseBody.value);
    }


}
