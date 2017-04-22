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
public class Parameter {

    private final String value;

    public Parameter(String value) {
        this.value = value;
    }

    public String orElse(String value) {
        return isPresent() ? this.value : value;
    }

    public Parameter orElseVal(String value) {
        String v = isPresent() ? this.value : value;
        return new Parameter(v);
    }

    public Integer asInt() {
        return isPresent() ? Integer.parseInt(value) : null;
    }

    public Double asDouble() {
        return isPresent() ? Double.parseDouble(value) : null;
    }

    public Float asFloat() {
        return isPresent() ? Float.parseFloat(value) : null;
    }

    public Boolean asBoolean() {
        return isPresent() ? Boolean.parseBoolean(value) : null;
    }

    public Long asLong() {
        return isPresent() ? Long.parseLong(value) : null;
    }

    public String asString() {
        return value;
    }

    public boolean isPresent() {
        return value != null && !value.isEmpty();
    }
}
