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

package io.joshworks.snappy.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Josh Gontijo on 3/24/17.
 */
public class ExecutorContainer {

    final Map<String, ThreadPoolExecutor> executors = new ConcurrentHashMap<>();
    final Map<String, ScheduledThreadPoolExecutor> schedulers = new ConcurrentHashMap<>();
    final String defaultExecutor;
    final String defaultScheduler;

    public ExecutorContainer(String defaultExecutor,
                             String defaultScheduler,
                             Map<String, ThreadPoolExecutor> execs,
                             Map<String, ScheduledThreadPoolExecutor> scheds) {
        this.defaultExecutor = defaultExecutor;
        this.defaultScheduler = defaultScheduler;

        executors.putAll(execs);
        schedulers.putAll(scheds);
    }


}
