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

/**
 * Created by Josh Gontijo on 3/17/17.
 */
public class Property {

    private final String value;

    public Property(String value) {
        this.value = value;
    }

    public String orElse(String value) {
        return isPresent() ? this.value : value;
    }

    public Property orElseVal(String value) {
        String v = isPresent() ? this.value : value;
        return new Property(v);
    }

    public int asInt() {
        return Integer.parseInt(value);
    }

    public double asDouble() {
        return Double.parseDouble(value);
    }

    public float asFloat() {
        return Float.parseFloat(value);
    }

    public boolean asBoolean() {
        return Boolean.parseBoolean(value);
    }

    public long asLong() {
        return Long.parseLong(value);
    }

    public String asString() {
        return value;
    }

    public boolean isPresent() {
        return value != null && !value.isEmpty();
    }
}
