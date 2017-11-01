package io.joshworks.snappy.it;

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.SimpleClient;
import io.undertow.server.handlers.CookieImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Josh Gontijo on 10/31/17.
 */
public class CookieTest {


    private static final String SERVER_URL = "http://localhost:9000";
    private static final String TEST_RESOURCE = "/echo";
    private static final String RESOURCE_PATH = SERVER_URL + TEST_RESOURCE;

    private static final String COOKIE_VALUE = "COOKIE-VAL-123";
    private static final String COOKIE_NAME = "COOKIE-NAME";

    @BeforeClass
    public static void setup() {

        enableTracer();
        get(TEST_RESOURCE, exchange -> {
            if(exchange.cookie(COOKIE_NAME) == null){
                exchange.cookie(new CookieImpl(COOKIE_NAME, COOKIE_VALUE)).end();
                return;
            }
            exchange.send(exchange.cookie(COOKIE_NAME).getValue(), "txt");
        });


        start();
    }

    @Test
    public void cookie() throws Exception {
        HttpResponse<String> response = SimpleClient.get(RESOURCE_PATH).asString();

        assertEquals(200, response.getStatus());

        String cookieHeader = response.getHeaders().getFirst("Set-Cookie");
        assertNotNull(cookieHeader);
        assertEquals(COOKIE_NAME + "=" + COOKIE_VALUE, cookieHeader);

        response = SimpleClient.get(RESOURCE_PATH).asString();

        assertEquals(200, response.getStatus());
        assertEquals(COOKIE_VALUE, response.getBody());

    }
}
