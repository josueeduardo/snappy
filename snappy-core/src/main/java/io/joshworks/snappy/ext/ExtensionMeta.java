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

package io.joshworks.snappy.ext;

/**
 * Created by Josh Gontijo on 3/25/17.
 */
public class ExtensionMeta {
    public final String name;
    public final String propertyPrefix;

    public ExtensionMeta(String name, String propertyPrefix) {
        this.name = name == null ? "(Name not provided)" : name;
        this.propertyPrefix = propertyPrefix;
    }
}
