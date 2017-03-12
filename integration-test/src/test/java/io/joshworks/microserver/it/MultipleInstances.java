package io.joshworks.microserver.it;

import io.joshworks.microserver.Config;
import io.joshworks.microserver.Microserver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.microserver.client.Clients.client;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/10/17.
 */
public class MultipleInstances {

    private static Microserver server1 = new Microserver(new Config().port(8080));
    private static Microserver server2 = new Microserver(new Config().port(8081));
    private static Microserver server3 = new Microserver(new Config().port(8082));

    @BeforeClass
    public static void start() {
        server1.get("/a", (exchange) -> {});
        server2.get("/b", (exchange) -> {});
        server3.get("/c", (exchange) -> {});

        server1.start();
        server2.start();
        server3.start();
    }

    @AfterClass
    public static void shutdown() {
        server1.stop();
        server2.stop();
        server3.stop();
    }

    @Test
    public void get_server1() {
        assertEquals(200, client().get("http://localhost:8080/a").getStatus());
    }

    @Test
    public void get_server2() {
        assertEquals(200, client().get("http://localhost:8081/b").getStatus());
    }

    @Test
    public void get_server3() {
        assertEquals(200, client().get("http://localhost:8082/c").getStatus());
    }

    @Test
    public void get_server1_crossResource() {
        assertEquals(404, client().get("http://localhost:8080/b").getStatus());
        assertEquals(404, client().get("http://localhost:8080/c").getStatus());
    }

    @Test
    public void get_server2_crossRequest() {
        assertEquals(404, client().get("http://localhost:8081/a").getStatus());
        assertEquals(404, client().get("http://localhost:8081/c").getStatus());
    }

    @Test
    public void get_server3_crossRequest() {
        assertEquals(404, client().get("http://localhost:8082/a").getStatus());
        assertEquals(404, client().get("http://localhost:8082/b").getStatus());
    }


}
