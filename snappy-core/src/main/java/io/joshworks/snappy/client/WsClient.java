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

package io.joshworks.snappy.client;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.WebSocketChannel;
import org.xnio.ChannelListener;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.net.URI;

/**
 * Created by Josh Gontijo on 3/20/17.
 */
public class WsClient {

    private static XnioWorker worker;

    private WsClient() {

    }

    public static WebSocketChannel connect(URI uri, ChannelListener<? super WebSocketChannel> clientEndpoint) throws IOException {
        WebSocketChannel webSocketChannel = new WebSocketClient.ConnectionBuilder(
                getWorker(),
                new DefaultByteBufferPool(false, 2048),
                uri)
                .connect().get();


        webSocketChannel.getReceiveSetter().set(clientEndpoint);
        webSocketChannel.resumeReceives();
        return webSocketChannel;
    }

    private static synchronized XnioWorker getWorker() throws IOException {
        if (worker == null) {
            worker = Xnio.getInstance().createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, 2)
                    .set(Options.CONNECTION_HIGH_WATER, 1000000)
                    .set(Options.CONNECTION_LOW_WATER, 1000000)
                    .set(Options.WORKER_TASK_CORE_THREADS, 1)
                    .set(Options.WORKER_TASK_MAX_THREADS, 10)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true)
                    .getMap());
        }
        return worker;
    }

}
