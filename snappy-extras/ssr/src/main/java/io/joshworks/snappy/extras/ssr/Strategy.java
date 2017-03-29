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

package io.joshworks.snappy.extras.ssr;


import io.joshworks.snappy.extras.ssr.common.Instance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Josue Gontijo.
 */
public abstract class Strategy {
    AtomicInteger counter = new AtomicInteger();


    public static Strategy random() {
        return new Strategy() {
            @Override
            public Instance apply(List<Instance> configs) {
                int idx = ThreadLocalRandom.current().nextInt(0, configs.size() - 1);
                return configs.get(idx);
            }
        };
    }

    public static Strategy roundRobin() {
        return new Strategy() {
            @Override
            public Instance apply(List<Instance> configs) {
                int current = counter.getAndIncrement();
                if (current >= configs.size()) {
                    current = 0;
                    counter.set(0);
                }
                return configs.get(current);
            }
        };
    }

    abstract Instance apply(List<Instance> configs);

}
