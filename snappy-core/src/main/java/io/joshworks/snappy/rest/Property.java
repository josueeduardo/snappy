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

package io.joshworks.snappy.rest;

import java.util.Optional;

/**
 * Created by Josh Gontijo on 3/17/17.
 */
public class Property {

    private final String value;

    public Property(String value) {
        this.value = value;
    }

    public Optional<Integer> asInt() {
        return Optional.of(Integer.parseInt(value));
    }

    public Optional<Double> asDouble() {
        return Optional.of(Double.parseDouble(value));
    }

    public Optional<Float> asFloat() {
        return Optional.of(Float.parseFloat(value));
    }

    public Optional<Boolean> asBoolean() {
        return Optional.of(Boolean.parseBoolean(value));
    }

    public Optional<Long> asLong() {
        return Optional.of(Long.parseLong(value));
    }

    public Optional<String> asString() {
        return Optional.of(value);
    }
}
