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

package io.joshworks.snappy.extras.ssr.server.service;

import io.joshworks.snappy.extras.ssr.Instance;
import io.joshworks.snappy.extras.ssr.server.ws.SessionStore;
import io.joshworks.snappy.rest.RestException;
import io.joshworks.snappy.rest.RestExchange;


/**
 * Created by Josue on 23/08/2016.
 */
public class InstancesResource {

    private final ServiceControl control;

    private final SessionStore sessionStore;

    public InstancesResource(ServiceControl control, SessionStore sessionStore) {
        this.control = control;
        this.sessionStore = sessionStore;
    }

    public void updateServiceState(RestExchange exchange) {
        String instanceId = exchange.pathParameter("instanceId");
        Instance instance = exchange.body().asObject(Instance.class);

        if (instance == null || instance.getState() == null) { //it only supports state update as for now
            throw RestException.badRequest("Invalid instance");
        }

        Instance updated = control.updateInstanceState(instanceId, instance.getState());
        sessionStore.pushInstanceState(updated);

        exchange.status(204);
    }
}