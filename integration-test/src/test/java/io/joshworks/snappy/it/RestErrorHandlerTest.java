package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.rest.DefaultExceptionResponse;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.util.Headers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class RestErrorHandlerTest {

    private static SnappyServer server = new SnappyServer();

    private static final String EXCEPTION_MESSAGE = "SOME ERROR OCCURRED";

    @BeforeClass
    public static void start() {
        server.get("/error1", (exchange) -> {});
        server.get("/exception", (exchange) -> {
            throw new RuntimeException(EXCEPTION_MESSAGE);
        });

        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void unsupportedContentType() throws Exception {
        HttpResponse<DefaultExceptionResponse> response = RestClient.get("http://localhost:8080/error1")
                .header("Content-Type", "application/xml").asObject(DefaultExceptionResponse.class);

        assertEquals(415, response.getStatus());
        //default response type
        assertEquals(1, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).size());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).get(0));

        DefaultExceptionResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(415, body.getStatus());
        assertNotNull(body.getMessage());
    }

    @Test
    public void unsupportedAcceptedType() throws Exception {
        HttpResponse<DefaultExceptionResponse> response = RestClient.get("http://localhost:8080/error1")
                .header("Accept", "application/xml").asObject(DefaultExceptionResponse.class);

        assertEquals(415, response.getStatus());
        //default response type
        assertEquals(1, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).size());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).get(0));

        DefaultExceptionResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(415, body.getStatus());
        assertNotNull(body.getMessage());
    }

    @Test
    public void exceptionThrown() throws Exception {
        HttpResponse<DefaultExceptionResponse> response = RestClient.get("http://localhost:8080/exception").asObject(DefaultExceptionResponse.class);

        assertEquals(500, response.getStatus());
        //default response type
        assertEquals(1, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).size());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).get(0));

        DefaultExceptionResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals(EXCEPTION_MESSAGE, body.getMessage());
    }

}
