package io.joshworks.snappy.it;

import io.joshworks.snappy.Config;
import io.joshworks.snappy.Microserver;
import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
    public void get_server1() throws Exception{
        assertEquals(200, RestClient.get("http://localhost:8080/a").asString().getStatus());
    }

    @Test
    public void get_server2() throws Exception{
        assertEquals(200, RestClient.get("http://localhost:8081/b").asString().getStatus());
    }

    @Test
    public void get_server3() throws Exception{
        assertEquals(200, RestClient.get("http://localhost:8082/c").asString().getStatus());
    }

    @Test
    public void get_server1_crossResource() throws Exception{
        assertEquals(404, RestClient.get("http://localhost:8080/b").asString().getStatus());
        assertEquals(404, RestClient.get("http://localhost:8080/c").asString().getStatus());
    }

    @Test
    public void get_server2_crossRequest() throws Exception{
        assertEquals(404, RestClient.get("http://localhost:8081/a").asString().getStatus());
        assertEquals(404, RestClient.get("http://localhost:8081/c").asString().getStatus());
    }

    @Test
    public void get_server3_crossRequest() throws Exception{
        assertEquals(404, RestClient.get("http://localhost:8082/a").asString().getStatus());
        assertEquals(404, RestClient.get("http://localhost:8082/b").asString().getStatus());
    }


}
