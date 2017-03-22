package io.joshworks.snappy.it;

import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/11/17.
 */
public class BasePathTest {

    @BeforeClass
    public static void setup() {
        basePath("/v1");
        get("/test", (exchange) -> {
        });
        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }


    @Test
    public void getRequest() throws Exception {
        assertEquals(200, RestClient.get("http://localhost:9000/v1/test").asString().getStatus());
    }


}
