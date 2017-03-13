package io.joshworks.snappy.it;

import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.parser.MediaTypes.accepts;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/12/17.
 */
public class ConegTest {

    private static SnappyServer server = new SnappyServer();

    @BeforeClass
    public static void start() {
        server.basePath("/v1").get("/json", (exchange) -> {});
        server.basePath("/v1").get("/xml", (exchange) -> {}, accepts("application/xml"));
        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void supportedMediaType() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header("Accept", "application/json").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void acceptsAll() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header("Accept", "*/*").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void supportedMediaTypeWithCharset() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header("Accept", "application/json; charset=UTF-8").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void unsupportedMediaType() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header("Accept", "application/xml").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void supportedMediaType_with_provided_value() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/xml").header("Accept", "application/xml").asString().getStatus();
        assertEquals(200, responseStatus);
    }


}
