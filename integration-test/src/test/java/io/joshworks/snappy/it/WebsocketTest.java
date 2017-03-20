package io.joshworks.snappy.it;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

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

    private static final String WS_ENDPOINT = "ws://localhost:8080/ws";

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

        WebSocketChannel webSocketChannel = new WebSocketClient.ConnectionBuilder(
                wsWorker(),
                new DefaultByteBufferPool(false, 2048),
                URI.create(WS_ENDPOINT))
                .connect().get();

        //block until connects
        webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {
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
        webSocketChannel.resumeReceives();

        assertTrue(webSocketChannel.isOpen());
        latch.await(10, TimeUnit.SECONDS);
        assertNotNull(result.get());

    }



    private XnioWorker wsWorker() {
        try {
            return Xnio.getInstance().createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, 2)
                    .set(Options.CONNECTION_HIGH_WATER, 1000000)
                    .set(Options.CONNECTION_LOW_WATER, 1000000)
                    .set(Options.WORKER_TASK_CORE_THREADS, 30)
                    .set(Options.WORKER_TASK_MAX_THREADS, 30)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true)
                    .getMap());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


}
