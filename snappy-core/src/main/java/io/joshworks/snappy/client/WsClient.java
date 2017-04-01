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

import java.net.URI;

/**
 * Created by Josh Gontijo on 3/20/17.
 */
public class WsClient {

    private WsClient() {

    }

    public static WebSocketChannel connect(URI uri, ChannelListener<? super WebSocketChannel> clientEndpoint) {
        try {
            WebSocketChannel webSocketChannel = new WebSocketClient.ConnectionBuilder(
                    ClientWorker.getWorker(),
                    new DefaultByteBufferPool(false, 2048),
                    uri)
                    .connect().get();


            webSocketChannel.getReceiveSetter().set(clientEndpoint);
            webSocketChannel.resumeReceives();
            return webSocketChannel;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
