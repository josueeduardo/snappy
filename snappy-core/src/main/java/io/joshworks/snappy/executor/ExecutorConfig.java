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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by josh on 3/14/17.
 */
public class ExecutorConfig extends ExecutorConfigBase {

    private ThreadPoolExecutor executor;

    public ExecutorConfig(String name) {
        super(name);
    }

    public static ExecutorConfig withDefaults(String name) {
        ExecutorConfig defaultConfig = new ExecutorConfig(name);
        defaultConfig.executor = new ThreadPoolExecutor(0, 5, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        return defaultConfig;
    }

    public ExecutorConfig executor(ThreadPoolExecutor executor) {
        this.executor = executor;
        return this;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
