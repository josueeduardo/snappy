package io.joshworks.snappy.it;

import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/12/17.
 */
public class MethodNotAllowedTest {

    @BeforeClass
    public static void setup() {
        basePath("/v1");
        get("/sample", (exchange) -> {
        });
        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void validGet() throws Exception {
        int responseStatus = RestClient.get("http://localhost:9000/v1/sample").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void invalidPut() throws Exception {
        int responseStatus = RestClient.put("http://localhost:9000/v1/sample").asString().getStatus();
        assertEquals(405, responseStatus);
    }

    @Test
    public void invalidPost() throws Exception {
        int responseStatus = RestClient.post("http://localhost:9000/v1/sample").asString().getStatus();
        assertEquals(405, responseStatus);
    }

    @Test
    public void invalidDelete() throws Exception {
        int responseStatus = RestClient.delete("http://localhost:9000/v1/sample").asString().getStatus();
        assertEquals(405, responseStatus);
    }

}
