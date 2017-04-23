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

package io.joshworks.snappy.examples.ls;

import io.joshworks.snappy.extensions.mvstore.H2MvStore;
import io.joshworks.snappy.rest.RestExchange;

import java.util.Map;
import java.util.UUID;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 4/22/17.
 */
public class UserResource {

    private Map<String, User> users;

    public UserResource() {
        onStart(() -> users = H2MvStore.of("users", String.class, User.class));
    }

    public void create(RestExchange exchange) {
        User user = exchange.body().asObject(User.class);
        if(user != null) {
            user.setId(UUID.randomUUID().toString().substring(0,9));
            users.put(user.getId(), user);
        }
    }

    public void getAll(RestExchange exchange) {
        exchange.send(users.values());
    }

    public void getById(RestExchange exchange) {
        String userId = exchange.pathParameter("userId");
        User found = users.get(userId);
        if(found == null) {
            exchange.status(404);
            return;
        }
        exchange.send(found);
    }
}
