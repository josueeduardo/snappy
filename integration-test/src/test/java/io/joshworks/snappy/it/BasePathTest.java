package io.joshworks.snappy.it;

import io.joshworks.snappy.Microserver;
import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void getRequest() throws Exception {
        assertEquals(200, RestClient.get("http://localhost:8080/v1/test").asString().getStatus());
    }


}
