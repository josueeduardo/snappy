package io.joshworks.microserver.it;

import io.joshworks.microserver.Microserver;
import io.joshworks.microserver.rest.RestExchange;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static io.joshworks.microserver.Endpoint.path;
import static io.joshworks.microserver.client.Clients.client;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/11/17.
 */
public class GroupTest {

    private static Microserver server = new Microserver();
    private static final String SERVER_URL = "http://localhost:8080";

    @BeforeClass
    public static void start() {
        path("/")
                .get("/a", GroupTest::sendPath)
                .group(path("/groupA").get("/b", GroupTest::sendPath))
                .group(path("/groupB").get("/c", GroupTest::sendPath))
                .group(path("/groupC")
                        .group(path("/d").get("/e", GroupTest::sendPath))
                        .group(path("/f").get("/{foo}", GroupTest::sendPath))
                        .group(path("/multi/path").get("/{bar}", GroupTest::sendPath)));


        server.start();
    }

    private static void sendPath(RestExchange exchange) {
        exchange.send(exchange.httpServerExchange.getRequestPath(), "text/plain");
    }


    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void root() {
        String path = "/a";
        assertPathRouting(path);
    }

    @Test
    public void groupA() {
        String path = "/groupA/b";
        assertPathRouting(path);
    }

    @Test
    public void groupB() {
        String path = "/groupB/c";
        assertPathRouting(path);
    }

    @Test
    public void groupC() {
        String path = "/groupC/d/e";
        assertPathRouting(path);
    }

    @Test
    public void groupC_singlePathParam() {
        String path = "/groupC/f/foo";
        assertPathRouting(path);
    }

    @Test
    public void groupC_muliplePath() {
        String path = "/groupC/multi/path/NAME";
        assertPathRouting(path);
    }

    private void assertPathRouting(String path) {
        Response response = client().get(SERVER_URL + path);
        assertEquals(200, response.getStatus());
        assertEquals(path, response.readEntity(String.class));
    }
}
