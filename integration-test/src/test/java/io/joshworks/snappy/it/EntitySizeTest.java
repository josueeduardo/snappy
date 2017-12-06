package io.joshworks.snappy.it;

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.JsonNode;
import io.joshworks.restclient.http.Unirest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 7/6/17.
 */
public class EntitySizeTest {

    private static final String SERVER_URL = "http://localhost:9000";

    private static final String MESSAGE = "0123456789";
    private static final long MAX_ENTITY_SIZE = MESSAGE.length() - 1;
    private static final long MULTIPART_MAX_ENTITY_SIZE = MESSAGE.length() - 2;



    @BeforeClass
    public static void setup() {

        maxEntitySize(MAX_ENTITY_SIZE);
        multipart("/form", exchange -> System.out.println(exchange.partNames()));
        multipart("/multipart", exchange -> System.out.println(exchange.partNames()));
        multipart("/withLimit", exchange -> System.out.println(exchange.partNames()), MAX_ENTITY_SIZE - 1);
        post("/plain", exchange -> System.out.println(exchange.body().asString()));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void limitFormParam() throws Exception {

        HttpResponse<JsonNode> response = Unirest.post(SERVER_URL + "/form")
                .field("message", MESSAGE)
                .asJson();

        assertEquals(500, response.getStatus());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getObject().getLong("id") > 0); //just to cause a call to getLong
    }

    @Test
    public void withFormLimit() throws Exception {

        HttpResponse<JsonNode> response = Unirest.post(SERVER_URL + "/form")
                .field("message", MESSAGE.substring(0, MESSAGE.length() - 5))
                .asJson();

        assertEquals(500, response.getStatus());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getObject().getLong("id") > 0); //just to cause a call to getLong
    }

    @Test
    public void limitPlainText() throws Exception {

        HttpResponse<JsonNode> response = Unirest.post(SERVER_URL + "/plain")
                .body(MESSAGE)
                .asJson();

        assertEquals(500, response.getStatus());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getObject().getLong("id") > 0); //just to cause a call to getLong
    }

}
