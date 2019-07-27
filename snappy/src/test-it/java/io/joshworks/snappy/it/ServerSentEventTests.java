package io.joshworks.snappy.it;

import io.joshworks.stream.client.StreamClient;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Josh Gontijo on 7/6/17.
 */
public class ServerSentEventTests {

    @Test
    public void handler() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> receivedMessage = new AtomicReference<>();

        String message = "yolo";
        try {
            sse("/sse", (connection, lastEventId) -> {
                connection.send(message);
            });
            start();

            StreamClient.sse("http://localhost:9000/sse")
                    .onEvent(e -> {
                        receivedMessage.set(e.data);
                        latch.countDown();
                    }).connect();

            if(!latch.await(5, TimeUnit.SECONDS)) {
                fail("No message received");
            }
            assertEquals(message, receivedMessage.get());

        } finally {
            stop();
        }
    }

}
