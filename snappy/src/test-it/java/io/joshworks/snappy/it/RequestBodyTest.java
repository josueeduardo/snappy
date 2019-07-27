package io.joshworks.snappy.it;

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static io.joshworks.snappy.SnappyServer.post;
import static io.joshworks.snappy.SnappyServer.start;

public class RequestBodyTest {

    private static final String SERVER_URL = "http://localhost:9000";

    @Test
    public void setOf() {
        post("/set", exchange -> {

            Set<String> strings = exchange.body().asSetOf(String.class);
            exchange.send(strings);

        });
        start();

        HttpResponse<String> response = Unirest.post(SERVER_URL + "/set")
                .header("Content-Type", "application/json")
                .body(Arrays.asList("a", "b"))
                .asString();

        System.out.println(response);


    }
}
