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

import io.joshworks.snappy.client.sse.EventData;
import io.joshworks.snappy.client.sse.SseClientCallback;
import io.joshworks.snappy.client.sse.SSEConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 3/31/17.
 */
public class SseClient {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    private SseClient() {

    }

    public static SSEConnection connect(String url, Consumer<EventData> callback) {
        return connect(url, new SseClientCallback() {
            @Override
            public void onEvent(EventData data) {
                callback.accept(data);
            }
        });
    }

    public static SSEConnection connect(String url, SseClientCallback callback) {
        SSEConnection connection = new SSEConnection(url, callback, ClientWorker.getWorker());
        connection.connect();
        return connection;
    }





}
