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

package io.joshworks.snappy.extras.store;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Josh Gontijo on 3/28/17.
 */
public class LocalStore {

    private static final Logger logger = LoggerFactory.getLogger(LocalStore.class);

    private static Manager manager;
    private static DatabaseOptions options;

    private static final Gson gson = new Gson();

    static void init(Manager manager, DatabaseOptions options) {
        LocalStore.manager = manager;
        LocalStore.options = options;
    }

    public static Database getDatabase(String name) {
        try {
            return manager.openDatabase(name, options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Manager getManager() {
        return manager;
    }

    public static void create(String databaseName, String id, Object object) {
        try {
            Map<String, Object> objectMap = toJsonMap(object);
            Database database = getDatabase(databaseName);
            Document document = database.getDocument(id);
            document.putProperties(objectMap);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void update(String databaseName, String id, Object object) {
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

    public static <T> Map<String, T> getAll(String databaseName, Class<T> type) {
        try {
            Map<String, T> results = new HashMap<>();
            Query query = getDatabase(databaseName).createAllDocumentsQuery();
            QueryEnumerator result = query.run();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                results.put(row.getDocumentId(), fromJson(row.getDocument(), type));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(String databaseName, String id, Class<T> type) {
        Document doc = getDatabase(databaseName).getDocument(id);
        return fromJson(doc, type);
    }

    public static SavedRevision merge(String databaseName, String id, Map<String, Object> newValues) {
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

    public static boolean delete(String databaseName, String id) {
        try {
            Document doc = getDatabase(databaseName).getDocument(id);
            return doc.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJson(Document doc, Class<T> type) {
        Map<String, Object> properties = doc.getProperties();
        JsonElement jsonElement = gson.toJsonTree(properties);
        return gson.fromJson(jsonElement, type);
    }

    private static Map<String, Object> toJsonMap(Object data) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(gson.toJson(data), type);
    }

}
