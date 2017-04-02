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

import io.joshworks.snappy.executor.AppExecutors;
import io.joshworks.snappy.rest.RestException;
import io.undertow.client.ClientCallback;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientExchange;
import io.undertow.client.ClientRequest;
import io.undertow.client.UndertowClient;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.ChannelListener;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.Channel;
import java.util.concurrent.TimeUnit;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 4/1/17.
 */
public class SSEConnection {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    final String url;
    final SseClientCallback callback;
    private XnioWorker worker;
    private ClientConnection connection;
    String lastEventId;

    public SSEConnection(String url, SseClientCallback callback, XnioWorker worker) {
        this.url = url;
        this.callback = callback;
        this.worker = worker;
    }

    public void connect() {
        try {
            if (connection != null) {
                return;
            }
            connection = UndertowClient.getInstance().connect(
                    URI.create(url),
                    worker,
                    new DefaultByteBufferPool(false, 8192),
                    OptionMap.EMPTY)
                    .get();


            final ClientRequest request = new ClientRequest().setMethod(Methods.GET).setPath(url);
            request.getRequestHeaders().put(Headers.CONNECTION, "keep-alive");
            request.getRequestHeaders().put(Headers.ACCEPT, "text/event-stream");
            request.getRequestHeaders().put(Headers.HOST, url); //TODO get from URI
//            request.getRequestHeaders().put(Headers.ORIGIN, "http://localhost");
            if (lastEventId != null && !lastEventId.isEmpty()) {
                request.getRequestHeaders().put(HttpString.tryFromString("Last-Event-ID"), lastEventId);
            }

            connection.sendRequest(request, createClientCallback());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the this connection and return the Last-Event-ID
     *
     * @return Last-Event-ID if any
     */
    public String close() {
        if (connection != null) {
            IoUtils.safeClose(connection);
            connection = null;
            callback.onClose();
        }
        return lastEventId;
    }

    public boolean isOpen() {
        return connection != null && connection.isOpen();
    }

    void retryAfter(long timeMilli) {
        logger.debug("Reconnecting after {}ms", timeMilli);
        AppExecutors.schedule(this::connect, timeMilli, TimeUnit.MILLISECONDS);
    }

    private ClientCallback<ClientExchange> createClientCallback() {
        UTF8Output dataReader = new UTF8Output(new EventStreamParser(this));
        final EventStreamChannelListener listener = new EventStreamChannelListener(new DefaultByteBufferPool(false, 8192), dataReader);

        return new ClientCallback<ClientExchange>() {
            @Override
            public void completed(ClientExchange connectedExchange) {

                connectedExchange.setResponseListener(new ClientCallback<ClientExchange>() {
                    @Override
                    public void completed(ClientExchange result) {
                        int responseCode = result.getResponse().getResponseCode();
                        if (responseCode != 200) {
                            String status = result.getResponse().getStatus();
                            callback.onError(new RestException(responseCode, "Server returned [" + responseCode + " - " + status + "] after connecting"));
                        }
                        callback.onOpen();

                        result.getResponseChannel().getCloseSetter().set((ChannelListener<Channel>) channel -> callback.onClose());
                        listener.setup(result.getResponseChannel());

                        result.getResponseChannel().resumeReads();
                    }

                    @Override
                    public void failed(IOException e) {
                        callback.onError(e);
                    }

                });
            }

            @Override
            public void failed(IOException e) {
                callback.onError(e);
            }
        };
    }
}
