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

package io.joshworks.snappy.extras.ssr.client;


import io.joshworks.snappy.extras.ssr.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.joshworks.snappy.extras.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josue on 19/06/2016.
 */
public class ServiceStore {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    private static final Object LOCK = new Object();

    private static final Map<String, Set<Instance>> store = new ConcurrentHashMap<>();
    private static final List<ServiceEventListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private Set<String> links = new HashSet<>();


    //TODO implement
//    private static final Queue<Event> eventBuffer = new ConcurrentLinkedDeque<>();
//    private static final int eventBufferSize = 1; //TODO configurable

    public Instance get(String serviceName) {
        if (serviceName == null) {
            return null;
        }
        return get(serviceName, Strategy.roundRobin());
    }

    public Set<String> getServices() {
        return store.keySet();
    }

    public Instance get(String serviceName, Strategy strategy) {
        if (serviceName == null || !store.containsKey(serviceName)) {
            return null;
        }
        Set<Instance> instances = store.get(serviceName);
        if (instances == null) {
            return null;
        }

        Instance apply = strategy.apply(new ArrayList<>(instances));
        sendLink(apply.getName());

//        sentStats(instances);

        return apply;
    }

    public void addListener(ServiceEventListener listener) {
        listeners.add(listener);
    }

    private void addService(Instance instance) {
        synchronized (LOCK) {
            if (!store.containsKey(instance.getName())) {
                store.put(instance.getName(), new HashSet<>());
            }
            store.get(instance.getName()).add(instance);
        }
    }

    private void removeService(Instance instance) {
        synchronized (LOCK) {
            if (store.containsKey(instance.getName())) {
                store.get(instance.getName()).remove(instance);

                if (store.get(instance.getName()).isEmpty()) {
                    store.remove(instance.getName());
                }
            }

        }
    }

    private void sendLink(String target) {
//        if (!links.contains(target)) {
//            Client client = ClientBuilder.newBuilder().build();
//
//            Map<String, String> targetMap = new HashMap<>();
//            targetMap.put("target", target);
//
//            String path = "http://" + Configuration.getRegistryUrl();
//            client.target(path)
//                    .path("api")
//                    .path("services")
//                    .path(Configuration.getCurrentInstance().getName())
//                    .request()
//                    .async()
//                    .put(Entity.json(targetMap));
//
//        }
    }

//    private void sentStats(ServiceConfig config) {
//        if (session != null && session.isOpen()) {
//            eventBuffer.rest(new Event(EventType.SERVICE_USAGE, config));
//
//            if (eventBuffer.size() == eventBufferSize) {
//                //for loop, since it will iterate a fixed ammount of times
//                //regardless some other thread adding more elements
//                for (int i = 0; i <= eventBuffer.size(); i++) {
//                    Event poll = eventBuffer.poll();
//                    if (poll != null) {
//                        session.getAsyncRemote().sendObject(poll);
//                    }
//                }
//            }
//
//        }
//    }

    void proccessInstance(Instance instance) {
        if (instance == null || instance.getState() == null) {
            logger.warn("Invalid instance state");
            return;
        }

        if (Instance.State.UP.equals(instance.getState())) {
            addService(instance);
            listeners.forEach(l -> l.onConnect(instance));
        }
        if (Instance.State.DOWN.equals(instance.getState())
                || Instance.State.OUT_OF_SERVICE.equals(instance.getState())) {
            removeService(instance);
            listeners.forEach(l -> l.onDisconnect(instance));
        }
    }


    void newSession() {
        synchronized (LOCK) {
            store.clear();
            links.clear();
        }

        //mainly used to clear store stale data, call before sending data
        listeners.forEach(ServiceEventListener::newSession);
    }

}
