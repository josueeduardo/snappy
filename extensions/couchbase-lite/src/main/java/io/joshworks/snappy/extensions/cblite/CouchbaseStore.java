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

package io.joshworks.snappy.extensions.cblite;

import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseOptions;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.SavedRevision;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josh Gontijo on 3/28/17.
 */
public class CouchbaseStore<T> {

    private static Manager manager;
    private static DatabaseOptions options;
    private static final Gson gson = new Gson();

    static void init(Manager manager, DatabaseOptions options) {
        CouchbaseStore.manager = manager;
        CouchbaseStore.options = options;
    }

    private final Class<T> type;
    private final String databaseName;

    private CouchbaseStore(String databaseName, Class<T> type) {
        this.databaseName = databaseName;
        this.type = type;
    }

    public static <T> CouchbaseStore<T> of(String name, Class<T> type) {
        return new CouchbaseStore<>(name, type);
    }

    public Database getDatabase(String name) {
        try {
            return manager.openDatabase(name, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Manager getManager() {
        return manager;
    }

    public void create(String id, T object) {
        try {
            Map<String, Object> objectMap = toJsonMap(object);
            Database database = getDatabase(databaseName);
            Document document = database.getDocument(id);
            document.putProperties(objectMap);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void update(String id, T object) {
        try {
            Database database = getDatabase(databaseName);
            Document document = database.getDocument(id);
            Map<String, Object> properties = new HashMap<>(document.getProperties());

            Map<String, Object> objectMap = toJsonMap(object);
            properties.putAll(objectMap);

            document.putProperties(properties);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, T> getAll() {
        try {
            Map<String, T> results = new HashMap<>();
            Query query = getDatabase(databaseName).createAllDocumentsQuery();
            QueryEnumerator result = query.run();
            while (result.hasNext()) {
                QueryRow row = result.next();
                results.put(row.getDocumentId(), fromJson(row.getDocument()));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T get(String id) {
        Document doc = getDatabase(databaseName).getDocument(id);
        return fromJson(doc);
    }

    public SavedRevision merge(String id, Map<String, Object> newValues) {
        try {
            Document doc = getDatabase(databaseName).getDocument(id);
            return doc.update(newRevision -> {
                Map<String, Object> properties = newRevision.getUserProperties();
                properties.putAll(newValues);
                newRevision.setUserProperties(properties);
                return true;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(String id) {
        try {
            Document doc = getDatabase(databaseName).getDocument(id);
            return doc.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private T fromJson(Document doc) {
        Map<String, Object> properties = doc.getProperties();
        JsonElement jsonElement = gson.toJsonTree(properties);
        return gson.fromJson(jsonElement, type);
    }

    private Map<String, Object> toJsonMap(T data) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(gson.toJson(data), type);
    }

}
