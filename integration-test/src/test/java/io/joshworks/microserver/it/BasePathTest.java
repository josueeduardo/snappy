package io.joshworks.microserver.it;

import io.joshworks.microserver.Microserver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static io.joshworks.microserver.client.Clients.client;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/11/17.
 */
public class BasePathTest {

    private static Microserver server = new Microserver();

    @BeforeClass
    public static void start() {
        server.basePath("/v1").get("/test", (exchange) -> {
        });
        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }


    @Test
    public void getRequest() {
        Response response = client().get("http://localhost:8080/v1/test");
        assertEquals(200, response.getStatus());
    }


}
