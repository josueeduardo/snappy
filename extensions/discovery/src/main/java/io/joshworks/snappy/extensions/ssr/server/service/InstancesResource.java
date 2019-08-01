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
import io.joshworks.snappy.http.HttpException;
import io.joshworks.snappy.http.Request;
import io.joshworks.snappy.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Josue on 23/08/2016.
 */
public class InstancesResource {

    private final ServiceControl control;

    private static final Logger logger = LoggerFactory.getLogger(InstancesResource.class);

    public InstancesResource(ServiceControl control) {
        this.control = control;
    }

    public Response register(Request request) {
        Instance instance = request.body().asObject(Instance.class);

        if (instance == null || instance.getState() == null) { //it only supports state update as for now
            throw HttpException.badRequest("Invalid instance");
        }

        Instance registered = control.register(instance);
//        sessionStore.pushInstanceState(registered);

        return Response.created().body(registered);
    }

    public Response updateServiceState(Request request) {
        String instanceId = request.pathParameter("instanceId");
        Instance instance = request.body().asObject(Instance.class);

        if (instance == null || instance.getState() == null) { //it only supports state update as for now
            throw HttpException.badRequest("Invalid instance");
        }

        try {
            Instance updated = control.updateInstanceState(instanceId, instance.getState());

        } catch (Exception ex) {
            logger.warn("Could not update instance {}", instanceId);
        }
//        sessionStore.pushInstanceState(updated);

        return Response.withStatus(204);
    }
}
