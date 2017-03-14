package io.joshworks.snappy.it;

import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/12/17.
 */
public class MethodNotAllowedTest {

    private static SnappyServer server = new SnappyServer();

    @BeforeClass
    public static void start() {
        server.basePath("/v1");
        server.get("/sample", (exchange) -> {});
        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void validGet() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/sample").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void invalidPut() throws Exception {
        int responseStatus = RestClient.put("http://localhost:8080/v1/sample").asString().getStatus();
        assertEquals(405, responseStatus);
    }

    @Test
    public void invalidPost() throws Exception {
        int responseStatus = RestClient.post("http://localhost:8080/v1/sample").asString().getStatus();
        assertEquals(405, responseStatus);
    }

    @Test
    public void invalidDelete() throws Exception {
        int responseStatus = RestClient.delete("http://localhost:8080/v1/sample").asString().getStatus();
        assertEquals(405, responseStatus);
    }

}
