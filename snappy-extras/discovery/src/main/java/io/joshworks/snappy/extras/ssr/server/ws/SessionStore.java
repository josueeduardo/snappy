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

package io.joshworks.snappy.extras.ssr.server.ws;


import io.joshworks.snappy.extras.ssr.Instance;
import io.undertow.server.session.Session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Josue on 22/08/2016.
 */
public class SessionStore {

    //TODO register WS clients sessions here
    private static final Map<String, Set<Session>> clients = new ConcurrentHashMap<>();

    public synchronized void addSession(String service, Session session) {
        if (!clients.containsKey(service)) {
            clients.put(service, new HashSet<>());
        }
        clients.get(service).add(session);
    }

    public synchronized void removeSession(String service, Session session) {
        if (!clients.containsKey(service)) {
            return;
        }
        clients.get(service).remove(session);
    }

    /**
     * Sends the newly registered service to all already available instances
     * If the new instance is not discoverable, nothing will happen
     * @param registered
     */
    public void pushInstanceState(Instance registered) {
        if (!registered.isDiscoverable()) {//do not send non discoverable service to the clients
            return;
        }
//        //send to all other services except current session
//        clients.entrySet().stream()
//                .flatMap(l -> l.getValue().stream())
//                .forEach(s -> {
//                    if (s.isOpen()) {
//                        s.getAsyncRemote().sendObject(registered); //TODO filter by dependency ?
//                    }
//                });
    }

}
