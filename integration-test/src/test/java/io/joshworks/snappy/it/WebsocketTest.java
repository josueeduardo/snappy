package io.joshworks.snappy.it;

import io.joshworks.snappy.client.WsClient;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by josh on 3/10/17.
 */
public class WebsocketTest {

    private static final String WS_ENDPOINT = "ws://localhost:8000/ws";

    private static final String message = "Yolo";

    @BeforeClass
    public static void setup() {

        websocket("/ws", (exchange, channel) -> {WebSockets.sendText(message, channel, null); channel.resumeReceives();});

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void ws() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> result = new AtomicReference<>();

        WebSocketChannel webSocketChannel = WsClient.connect(URI.create(WS_ENDPOINT), new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                result.set(message.getData());
                channel.sendClose();
                latch.countDown();
            }


            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                error.printStackTrace();
                latch.countDown();
            }
        });

        assertTrue(webSocketChannel.isOpen());
        latch.await(10, TimeUnit.SECONDS);
        assertNotNull(result.get());

    }

}
