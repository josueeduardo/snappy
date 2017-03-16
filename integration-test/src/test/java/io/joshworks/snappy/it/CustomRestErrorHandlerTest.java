package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.it.util.SampleData;
import io.joshworks.snappy.rest.MediaType;
import io.undertow.util.Headers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class CustomRestErrorHandlerTest {

    private static SnappyServer server = new SnappyServer();

    private static final SampleData payload = new SampleData("Yolo");

    private static final CustomExceptionBody exceptionBody_1 = new CustomExceptionBody("123", 123L);
    private static final CustomExceptionBody exceptionBody_2 = new CustomExceptionBody("456", 456L);
    private static final int responseStatus_1 = 401;
    private static final int responseStatus_2 = 405;

    @BeforeClass
    public static void start() {
        server.exception(Exception.class, (e, exchange) -> exchange.status(responseStatus_1).send(exceptionBody_1));
        server.exception(UnsupportedOperationException.class, (e, exchange) -> exchange.status(responseStatus_2).send(exceptionBody_2));
        //custom media type
        server.exception(NumberFormatException.class, (e, exchange) -> exchange.status(responseStatus_2).send(exceptionBody_2, MediaType.TEXT_PLAIN_TYPE));

        server.get("/custom-handler-1", (exchange) -> {
            throw new RuntimeException("Some error");
        });

        server.get("/custom-handler-2", (exchange) -> {
            throw new UnsupportedOperationException("Some other error");
        });

        server.get("/custom-handler-3-mediaType", (exchange) -> {
            throw new NumberFormatException("Some error with custom media type");
        });

        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }


    @Test
    public void exceptionThrown() throws Exception {
        HttpResponse<CustomExceptionBody> response = RestClient.get("http://localhost:8080/custom-handler-1").asObject(CustomExceptionBody.class);

        assertEquals(responseStatus_1, response.getStatus());

        CustomExceptionBody body = response.getBody();
        assertNotNull(body);
        assertEquals(exceptionBody_1.getUuid(), body.getUuid());
        assertEquals(exceptionBody_1.getTime(), body.getTime());
    }

    @Test
    public void exceptionThrownExactMatch() throws Exception {
        HttpResponse<CustomExceptionBody> response = RestClient.get("http://localhost:8080/custom-handler-2").asObject(CustomExceptionBody.class);

        assertEquals(responseStatus_2, response.getStatus());

        CustomExceptionBody body = response.getBody();
        assertNotNull(body);
        assertEquals(exceptionBody_2.getUuid(), body.getUuid());
        assertEquals(exceptionBody_2.getTime(), body.getTime());
    }

    @Test
    public void exceptionProvidedMediaType() throws Exception {
        HttpResponse<String> response = RestClient.get("http://localhost:8080/custom-handler-3-mediaType")
                .asString();

        assertEquals(responseStatus_2, response.getStatus());
        //custom response type
        assertEquals(1, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).size());
        assertEquals(MediaType.TEXT_PLAIN, response.getHeaders().get(Headers.CONTENT_TYPE.toString()).get(0));

        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(exceptionBody_2.getUuid()));
        assertTrue(response.getBody().contains(""+exceptionBody_2.getTime()));
    }

    private static class CustomExceptionBody {
        private String uuid;
        private long time;

        public CustomExceptionBody(String uuid, long time) {

            this.uuid = uuid;
            this.time = time;
        }


        public String getUuid() {
            return uuid;
        }

        public long getTime() {
            return time;
        }

        //this is used to compare string result from the text/plain media type
        @Override
        public String toString() {
            return "CustomExceptionBody{" +
                    "uuid='" + uuid + '\'' +
                    ", time=" + time +
                    '}';
        }
    }

}
