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

package io.joshworks.snappy.property;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by josh on 3/10/17.
 */
public final class PropertyKeys {

    public static final String PROPERTIES_NAME = "snappy.properties";

    public static final Set<String> RESERVED_PREFIXES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("http", "xnio", "executor", "scheduler")));

    //http
    public static final String HTTP_PORT = "http.port";
    public static final String HTTP_BIND_ADDRESS = "http.bind";
    public static final String HTTP_TRACER = "http.tracer";
    public static final String HTTP_METRICS = "http.metrics";
    public static final String TCP_NO_DELAY = "tcp.nodelay";

    //core server
    public static final String XNIO_IO_THREADS = "xnio.io.threads";
    public static final String XNIO_MAX_WORKER_THREAD = "xnio.worker.maxThread";
    public static final String XNIO_CORE_WORKER_THREAD = "xnio.worker.coreThread";

    //app executors
    public static final String EXECUTOR_PREFIX = "executor.";
    public static final String SCHEDULER_PREFIX = "scheduler.";
    public static final String EXECUTOR_CORE_POOL_SIZE = "core";
    public static final String EXECUTOR_MAX_POOL_SIZE = "max";

}
