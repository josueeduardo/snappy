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

/**
 * Created by Josh Gontijo on 4/1/17.
 * Manages XnioWorker for SSE and Websockers
 */
public class ClientWorker {

    private static final OptionMap.Builder DEFAULT = OptionMap.builder()
            .set(Options.WORKER_IO_THREADS, 5)
            .set(Options.TCP_NODELAY, true)
            .set(Options.KEEP_ALIVE, true);

    private static XnioWorker worker;

    public synchronized static void configure(OptionMap.Builder builder) throws IOException {
        if(builder == null) {
            builder = DEFAULT;
        }
        builder.set(Options.WORKER_NAME, "CLIENT-WORKER");
        worker = Xnio.getInstance().createWorker(builder.getMap());
    }

    static XnioWorker getWorker() {
        if(worker == null) {
            throw new IllegalStateException("Client worker not configured");
        }
        return worker;
    }

}
