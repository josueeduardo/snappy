package io.joshworks.snappy.it;

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.JsonNode;
import io.joshworks.restclient.http.Unirest;
import io.joshworks.snappy.http.Request;
import io.joshworks.snappy.http.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.maxEntitySize;
import static io.joshworks.snappy.SnappyServer.post;
import static io.joshworks.snappy.SnappyServer.start;
import static io.joshworks.snappy.SnappyServer.stop;
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
        post("/form", EntitySizeTest::printAndReturn);
        post("/multipart", EntitySizeTest::printAndReturn);
        //FIXME add limit
//        post("/withLimit", EntitySizeTest::printAndReturn, MAX_ENTITY_SIZE - 1);
        post("/plain", EntitySizeTest::printAndReturn);

        start();
    }

    private static Response printAndReturn(Request req) {
        System.out.println(req.multiPartBody().partNames());
        return Response.ok();
    }

    @AfterClass
    public static void shutdown() {
        stop();
        Unirest.close();
    }

    @Test
    public void limitFormParam() {

        HttpResponse<JsonNode> response = Unirest.post(SERVER_URL + "/form")
                .field("message", MESSAGE)
                .asJson();

        Assert.assertEquals(500, response.getStatus());
        assertNotNull(response.body());
        assertTrue(response.body().getObject().getLong("id") > 0); //just to cause a call to getLong
    }

    @Test
    public void withFormLimit() {

        HttpResponse<JsonNode> response = Unirest.post(SERVER_URL + "/form")
                .field("message", MESSAGE.substring(0, MESSAGE.length() - 5))
                .asJson();

        Assert.assertEquals(500, response.getStatus());
        assertNotNull(response.body());
        assertTrue(response.body().getObject().getLong("id") > 0); //just to cause a call to getLong
    }

    @Test
    public void limitPlainText() {

        HttpResponse<JsonNode> response = Unirest.post(SERVER_URL + "/plain")
                .body(MESSAGE)
                .asJson();

        Assert.assertEquals(500, response.getStatus());
        assertNotNull(response.body());
        assertTrue(response.body().getObject().getLong("id") > 0); //just to cause a call to getLong
    }

}
