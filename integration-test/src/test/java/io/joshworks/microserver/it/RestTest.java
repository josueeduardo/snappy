package io.joshworks.microserver.it;

import io.joshworks.microserver.Endpoint;
import io.joshworks.microserver.Microserver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.microserver.client.Clients.client;
import static org.junit.Assert.assertEquals;

/**
 * Created by josh on 3/10/17.
 */
public class RestTest {

    private static Microserver server = new Microserver();

    @BeforeClass
    public static void start(){
        Endpoint.post("/echo", (exchange) -> exchange.send(exchange.body(User.class)));
        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void sample() {
        User payload = new User("Josh");
        User response = client().post("http://localhost:8080/echo", payload, User.class);
        assertEquals(payload.name, response.name);
    }


    class User {
        final String name;

        public User(String name) {
            this.name = name;
        }
    }


}
