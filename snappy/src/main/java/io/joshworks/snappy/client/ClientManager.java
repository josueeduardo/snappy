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

import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;

import static io.joshworks.snappy.SnappyServer.onShutdown;

/**
 * Created by Josh Gontijo on 4/2/17.
 */
public class ClientManager {

    private static final String CLIENT_WORKER_NAME = "client-worker";

    private static final OptionMap.Builder DEFAULT = OptionMap.builder()
            .set(Options.WORKER_IO_THREADS, 5)
            .set(Options.TCP_NODELAY, true)
            .set(Options.KEEP_ALIVE, true);

    private static XnioWorker worker;

    private static UrlLookup lookup = new UrlLookup();

    public static void init() {
        RestClient.init();

        onShutdown(() -> {
            RestClient.shutdown();
            worker.shutdown();
        });

    }

    public synchronized static void configureWorker(OptionMap.Builder builder) throws IOException {
        if (builder == null) {
            builder = DEFAULT;
        }
        builder.set(Options.WORKER_NAME, CLIENT_WORKER_NAME);
        worker = Xnio.getInstance().createWorker(builder.getMap());
    }

    public static void configureClientUrlLookup(UrlLookup lookup) {
        ClientManager.lookup = lookup;
    }

    static XnioWorker getWorker() {
        if (worker == null) {
            try {
                worker = Xnio.getInstance().createWorker(DEFAULT.getMap());
            } catch (Exception e) {
                throw new RuntimeException("Could not build client worker", e);
            }
        }
        return worker;
    }

    static String lookup(String original) {
        return lookup.getUrl(original);
    }

}
