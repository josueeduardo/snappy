package io.joshworks.microserver.it;

import io.joshworks.microserver.Microserver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static io.joshworks.microserver.Endpoint.basePath;
import static io.joshworks.microserver.Endpoint.get;
import static io.joshworks.microserver.client.Clients.client;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/11/17.
 */
public class BasePathTest {

    private static Microserver server = new Microserver();
    private static final String SERVER_URL = "http://localhost:8080";
    private static final String BASE_PATH = "/v1";
    private static final String TEST_RESOURCE = "/test";
    private static final String RESOURCE_PATH = SERVER_URL + BASE_PATH + TEST_RESOURCE;


    private static final int RESPONSE_STATUS = 200;

    @BeforeClass
    public static void start() {
        basePath(BASE_PATH);
        get(TEST_RESOURCE, (exchange) -> exchange.status(200));
        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }


    @Test
    public void getRequest() {
        Response response = client().get(RESOURCE_PATH);
        assertEquals(RESPONSE_STATUS, response.getStatus());
    }



}
