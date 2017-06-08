package io.joshworks.snappy.sse.client.sse;

import io.undertow.server.DefaultByteBufferPool;
import org.junit.Test;
import org.xnio.channels.EmptyStreamSourceChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * Created by Josh Gontijo on 6/3/17.
 */
public class EventStreamChannelListenerTest {
    private static List<String> messages = Arrays.asList("data: 1\n\n", "data: 222222\n\n");
    private static CountDownLatch latch = new CountDownLatch(messages.size());

    @Test
    public void handleEvent() throws Exception {
        DefaultByteBufferPool defaultByteBufferPool = new DefaultByteBufferPool(false, 2048);
        UTF8Output utf8Output = new UTF8Output(new EventStreamParser(new SSEConnection(null, new TestListener(), null)));
        EventStreamChannelListener listener = new EventStreamChannelListener(defaultByteBufferPool, utf8Output);

        listener.handleEvent(new DummyStreamChannel(messages));

        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("Did not receive any message");
        }

    }

    public static class TestListener extends SseClientCallback {

        @Override
        public void onEvent(EventData data) {
            System.out.println(data);
            latch.countDown();
        }

        @Override
        public void onClose(String lastEventId) {

        }

        @Override
        public void onError(Exception e) {

        }
    }

    public static class DummyStreamChannel extends EmptyStreamSourceChannel {

        private final Queue<String> messages = new LinkedList<>();

        public DummyStreamChannel(List<String> contents) {
            super(null);
            messages.addAll(contents);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            String poll = messages.poll();
            byte[] bytes = poll == null ? new byte[0] : poll.getBytes();

            dst.clear();
            dst.put(bytes);
            return bytes.length;
        }

        @Override
        public void resumeReads() {

        }
    }
}