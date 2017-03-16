package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.it.util.SampleData;
import io.joshworks.snappy.rest.ExceptionResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        server.exception(Exception.class, (e) -> new ExceptionResponse(responseStatus_1, exceptionBody_1));
        server.exception(UnsupportedOperationException.class, (e) -> new ExceptionResponse(responseStatus_2, exceptionBody_2));

        server.get("/custom-handler-1", (exchange) -> {
            throw new RuntimeException("Some error");
        });

        server.get("/custom-handler-2", (exchange) -> {
            throw new UnsupportedOperationException("Some other error");
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
    }

}
