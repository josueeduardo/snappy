package com.josue.simpletow.discovery;


import com.josue.simpletow.discovery.common.Instance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Josue on 19/06/2016.
 */
public class ServiceStore {

    private static final Object LOCK = new Object();

    private static final Map<String, Set<Instance>> store = new ConcurrentHashMap<>();
    private Set<String> links = new HashSet<>();

    private static final List<ServiceEventListener> listeners = Collections.synchronizedList(new ArrayList<>());


    //TODO implement
//    private static final Queue<Event> eventBuffer = new ConcurrentLinkedDeque<>();
//    private static final int eventBufferSize = 1; //TODO configurable

    public Instance get(String serviceName) {
        return get(serviceName, Strategy.roundRobin());
    }

    public Set<String> getServices() {
        return store.keySet();
    }

    public Instance get(String serviceName, Strategy strategy) {
        if (!store.containsKey(serviceName)) {
            return null;
        }
        Set<Instance> instances = store.get(serviceName);
        if (instances == null) {
            return null;
        }

        Instance apply = strategy.apply(new ArrayList<Instance>(instances));
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
            }
            if (store.get(instance.getName()).isEmpty()) {
                store.remove(instance.getName());
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
//            String path = "http://" + Configurator.getRegistryUrl();
//            client.target(path)
//                    .path("api")
//                    .path("services")
//                    .path(Configurator.getCurrentInstance().getName())
//                    .request()
//                    .async()
//                    .put(Entity.json(targetMap));
//
//        }
    }

//    private void sentStats(ServiceConfig config) {
//        if (session != null && session.isOpen()) {
//            eventBuffer.add(new Event(EventType.SERVICE_USAGE, config));
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

    void onConnect(Instance instance) {
        addService(instance);
        listeners.forEach(l -> l.onConnect(instance));
    }

    void onDisconnect(Instance instance) {
        removeService(instance);
        listeners.forEach(l -> l.onDisconnect(instance));
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
