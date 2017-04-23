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

package io.joshworks.snappy.extensions.ssr.server.service;


import io.joshworks.snappy.extensions.ssr.Instance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Josue on 09/06/2016.
 */
public class Service implements Serializable {

    private String name;
    private final Map<String, Instance> instances = new HashMap<>();
    private final Set<String> links = new HashSet<>();

    public Service() {
    }

    public Service(String name) {
        this.name = name;
    }

    public Set<String> getLinks() {
        return links;
    }

    public Set<Instance> getInstances() {
        return new HashSet<>(instances.values());
    }

    public boolean containsInstance(String instanceId) {
        return instanceId != null && instances.values().stream().anyMatch(i -> i.getId().equals(instanceId));
    }

    public Instance addInstance(Instance newInstance) {
        instances.put(newInstance.resolveAddress(), newInstance);
        return newInstance;
    }

    public synchronized void removeInstance(String instanceId) {
        instances.values().removeIf(instance -> instance.getId().equals(instanceId));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;

        Service that = (Service) o;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}

