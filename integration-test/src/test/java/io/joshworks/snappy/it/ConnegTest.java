package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.rest.MediaType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import static io.joshworks.snappy.SnappyServer.basePath;
import static io.joshworks.snappy.SnappyServer.get;
import static io.joshworks.snappy.SnappyServer.start;
import static io.joshworks.snappy.SnappyServer.stop;
import static io.joshworks.snappy.parser.MediaTypes.consumes;
import static io.joshworks.snappy.parser.MediaTypes.produces;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by josh on 3/12/17.
 */
public class ConnegTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";

    @BeforeClass
    public static void setup() {
        basePath("/v1");
        get("/json", (exchange) -> {});
        get("/xml", (exchange) -> {}, consumes("application/xml"));
        get("/simple-mime", (exchange) -> {}, consumes("json"));

        //produces
        get("/produces-json", (exchange) -> {}, produces("application/json"));
        get("/produces-text", (exchange) -> {}, produces("text/plain"));

        get("/overridden", (exchange) -> {
            exchange.send("{}", MediaType.APPLICATION_JSON_TYPE);
        }, produces("text/plain"));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    // ----- SERVER CONSUMES -----
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
    public void unsupportedMediaType_withDefaults() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/json").header(ACCEPT, "application/xml").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void supportedMediaType_with_provided_value() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/xml").header(ACCEPT, "application/xml").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void simpleMime() throws Exception {
        HttpResponse<String> response = RestClient.get("http://localhost:8080/v1/simple-mime").header(ACCEPT, "application/json").asString();
        assertEquals(200, response.getStatus());
        assertNotNull(response.getHeaders().get(CONTENT_TYPE));
        assertEquals("application/json", response.getHeaders().get(CONTENT_TYPE).get(0));
    }

    @Test
    public void supportedMediaType_prefer_json() throws Exception {
        HttpResponse<String> stringHttpResponse = RestClient.get("http://localhost:8080/v1/xml")
                .header(ACCEPT, "application/json")
                .header(ACCEPT, "text/plain")
                .asString();
        assertHeaderEquals("application/json", stringHttpResponse);
    }

    @Test
    public void supportedMediaType_prefer_plain() throws Exception {
        HttpResponse<String> stringHttpResponse = RestClient.get("http://localhost:8080/v1/json")
                .header(ACCEPT, "text/plain")
                .header(ACCEPT, "application/json")
                .asString();
        assertEquals(200, stringHttpResponse.getStatus());
        assertHeaderEquals("text/plain", stringHttpResponse);
    }

    @Test
    public void validRequestPayloadMime() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/xml").header(CONTENT_TYPE, "application/xml").asString().getStatus();
        assertEquals(200, responseStatus);
    }

    @Test
    public void invalidRequestPayloadMime() throws Exception {
        int responseStatus = RestClient.get("http://localhost:8080/v1/xml").header(CONTENT_TYPE, "application/json").asString().getStatus();
        assertEquals(415, responseStatus);
    }

    @Test
    public void validRequestPayloadMime_with_consumes_sameType() throws Exception {
        HttpResponse<String> response = RestClient.get("http://localhost:8080/v1/json")
                .header(CONTENT_TYPE, "application/json")
                .header(ACCEPT, "application/json")
                .asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }


    // ----- SERVER PRODUCES -----
    @Test
    public void noAcceptSpecified_default_json() throws Exception {
        HttpResponse<String> response = RestClient.get("http://localhost:8080/v1/json").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }

    @Test
    public void producesTextPlain() throws Exception {
        HttpResponse<String> response = RestClient.get("http://localhost:8080/v1/produces-text").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("text/plain", response);
    }

    @Test
    public void producesJson() throws Exception {
        HttpResponse<String> response = RestClient.get("http://localhost:8080/v1/produces-json").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }

    @Test
    public void contentTypeOverridden() throws Exception {
        HttpResponse<String> response = RestClient.get("http://localhost:8080/v1/overridden").asString();
        assertEquals(200, response.getStatus());
        assertHeaderEquals("application/json", response);
    }

    private void assertHeaderEquals(String expected, HttpResponse response) {
        assertNotNull(response.getHeaders().get(CONTENT_TYPE));
        assertEquals(1, response.getHeaders().get(CONTENT_TYPE).size());
        assertEquals(expected, response.getHeaders().get(CONTENT_TYPE).get(0));
    }

}
