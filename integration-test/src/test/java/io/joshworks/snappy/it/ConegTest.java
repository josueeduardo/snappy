package io.joshworks.snappy.it;

import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.parser.MediaTypes.consumes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by josh on 3/12/17.
 */
public class ConegTest {

    private static SnappyServer server = new SnappyServer();

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";

    @BeforeClass
    public static void start() {
        server.basePath("/v1").get("/json", (exchange) -> {});
        server.basePath("/v1").get("/xml", (exchange) -> {}, consumes("application/xml"));
        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void supportedMediaType() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header(ACCEPT, "application/json").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void acceptsAll() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header(ACCEPT, "*/*").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void supportedMediaTypeWithCharset() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header(ACCEPT, "application/json; charset=UTF-8").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void unsupportedMediaType() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header(ACCEPT, "application/xml").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void supportedMediaType_with_provided_value() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/xml").header(ACCEPT, "application/xml").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void supportedMediaType_prefer_json() throws Exception {
        HttpResponse<String> stringHttpResponse = RestClient.get("http://localhost:8080/v1/xml")
                .header(ACCEPT, "application/json")
                .header(ACCEPT, "text/plain")
                .asString();
        Headers headers = stringHttpResponse.getHeaders();
        assertNotNull(headers.get(CONTENT_TYPE));
        assertEquals(1, headers.get(CONTENT_TYPE).size());
        assertEquals("application/json", headers.get(CONTENT_TYPE).iterator().next());
    }

    @Test
    public void supportedMediaType_prefer_plain() throws Exception {
        HttpResponse<String> stringHttpResponse = RestClient.get("http://localhost:8080/v1/json")
                .header(ACCEPT, "text/plain")
                .header(ACCEPT, "application/json")
                .asString();
        assertEquals(200, stringHttpResponse.getStatus());

        Headers headers = stringHttpResponse.getHeaders();
        assertNotNull(headers.get(CONTENT_TYPE));
        assertEquals(1, headers.get(CONTENT_TYPE).size());
        assertEquals("text/plain", headers.get(CONTENT_TYPE).iterator().next());
    }


}
