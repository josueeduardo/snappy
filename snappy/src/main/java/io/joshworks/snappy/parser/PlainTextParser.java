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

package io.joshworks.snappy.parser;

import io.joshworks.snappy.http.MediaType;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by josh on 3/6/17.
 */
public class PlainTextParser implements Parser {

    @Override
    public <T> T readValue(String value, Class<T> valueType) {
        throw new UnsupportedOperationException("Cannot convert " + valueType + " to " + mediaType());
    }

    @Override
    public <T> T readValue(String value, Type valueType) {
        throw new UnsupportedOperationException("Cannot convert " + valueType + " to " + mediaType());
    }

    @Override
    public String writeValue(Object input) {
        return String.valueOf(input);
    }

    @Override
    public Set<MediaType> mediaType() {
        HashSet<MediaType> types = new HashSet<>();
        types.add(MediaType.WILDCARD_TYPE);
        return types;
    }

}
