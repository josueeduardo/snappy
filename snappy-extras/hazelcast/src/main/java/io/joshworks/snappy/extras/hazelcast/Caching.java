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

package io.joshworks.snappy.extras.hazelcast;

import com.hazelcast.core.HazelcastInstance;

/**
 * Created by Josh Gontijo on 3/29/17.
 */
public class Caching {

    private static HazelcastInstance instance;

    static void init(HazelcastInstance instance) {
        Caching.instance = instance;
    }

    public static HazelcastInstance instance() {
        return instance;
    }


}
