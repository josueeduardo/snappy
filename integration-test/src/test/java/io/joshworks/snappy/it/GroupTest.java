package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.client.RestClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Josh Gontijo on 3/17/17.
 */
public class GroupTest {

    private static final String basePath = "http://localhost:8000";

    @BeforeClass
    public static void setup() {

        get("/a", (exchange) -> exchange.send(exchange.path(), "txt"));
        group("/groupA", () -> {
            get("/", (exchange) -> exchange.send(exchange.path(), "txt"));
            get("/b", (exchange) -> exchange.send(exchange.path(), "txt"));
            get("/c", (exchange) -> exchange.send(exchange.path(), "txt"));

            group("/groupB", () -> {
                get("/d", (exchange) -> exchange.send(exchange.path(), "txt"));
            });

            group("/{param}", () -> {
                get("/e", (exchange) -> exchange.send(exchange.path(), "txt"));
            });
        });

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void noGroup() throws Exception {
        assertPath("/a");
    }

    @Test
    public void withRootPath() throws Exception {
        assertPath("/groupA");
    }

    @Test
    public void firstLevelGroup() throws Exception {
        assertPath("/groupA/b");
    }

    @Test
    public void multipleNestedGroups() throws Exception {
        assertPath("/groupA/groupB/d");
    }

    @Test
    public void withGroupParam() throws Exception {
        assertPath("/groupA/YOLO/e");
    }

    private void assertPath(String path) throws Exception {
        HttpResponse<String> response = RestClient.get(basePath + path).asString();
        assertEquals(200, response.getStatus());
        assertEquals(path, response.getBody());
    }


}
