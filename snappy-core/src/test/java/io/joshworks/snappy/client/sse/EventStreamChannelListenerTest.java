/*
 * Copyright 2017 Josue Gontijo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.joshworks.snappy.client.sse;

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
 * Created by Josh Gontijo on 4/1/17.
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

    public static class TestListener implements SSEClientCallback {

        @Override
        public void onEvent(EventData data) {
            System.out.println(data);
            latch.countDown();
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