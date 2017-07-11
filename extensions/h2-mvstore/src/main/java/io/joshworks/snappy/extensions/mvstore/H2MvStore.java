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

package io.joshworks.snappy.extensions.mvstore;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.type.DataType;
import org.h2.mvstore.type.ObjectDataType;
import org.h2.mvstore.type.StringDataType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Josh Gontijo on 3/28/17.
 */
public class H2MvStore {

    private static final Set<Class<?>> defaultTypes = new HashSet<>(Arrays.asList(
            Integer.class,
            Byte.class,
            Short.class,
            Character.class,
            Long.class,
            Float.class,
            BigInteger.class,
            BigDecimal.class,
            Double.class,
            UUID.class,
            Date.class));

    private static MVStore store;

    private static final Map<String, Map> stores = new HashMap<>();

    static void init(MVStore store) {
        H2MvStore.store = store;
    }

    public static <U, T> Map<U, T> of(String name, Class<U> keyType, Class<T> valueType) {
        checkInitialized();
        if (stores.containsKey(name)) {
            return stores.get(name);
        }

        MVMap<U, T> mvMap = store.openMap(name, new MVMap.Builder<U, T>()
                .keyType(getDataType(keyType))
                .valueType(getDataType(valueType)));

        stores.put(name, mvMap);
        return mvMap;

    }

    public static <U, T> Map<U, T> of(String name, Class<U> keyType, Type valueType) {
        checkInitialized();
        if (stores.containsKey(name)) {
            return stores.get(name);
        }

        MVMap<U, T> mvMap = store.openMap(name, new MVMap.Builder<U, T>()
                .keyType(getDataType(keyType))
                .valueType(JsonSerializer.of(valueType)));

        stores.put(name, mvMap);
        return mvMap;
    }

    public MVStore store() {
        checkInitialized();
        return store;
    }

    private static void checkInitialized() {
        if (store == null) {
            throw new IllegalStateException("Store not initialized");
        }
    }

    private static DataType getDataType(Class<?> type) {
        if (String.class.equals(type)) {
            return new StringDataType();
        }
        if (defaultTypes.contains(type)) {
            return new ObjectDataType();
        }

        return JsonSerializer.of(type);
    }


}
