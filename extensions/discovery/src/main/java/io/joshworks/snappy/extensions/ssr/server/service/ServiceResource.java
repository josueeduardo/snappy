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


import io.joshworks.snappy.http.HttpExchange;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Josue on 09/06/2016.
 */
public class ServiceResource implements Serializable {


    private final ServiceControl control;

    public ServiceResource(ServiceControl control) {
        this.control = control;
    }

    public void getServices(HttpExchange exchange) {
        Set<Service> services = control.getServices();
        exchange.send(services);
    }


    public void getService(HttpExchange exchange) {
        Service service = control.getService(exchange.pathParameter("name"));
        exchange.send(service);
    }

    public void addLink(HttpExchange exchange)  {
        String source = exchange.pathParameter("name");
        String target = exchange.body().asJson().getObject().getString("target");

        control.addLink(source, target);
        exchange.send(204);
    }

//    @PUT
//    @Path("{name}")
//    public Response deleteUnavailableNodes(@PathParam("name") String name) throws Exception {
//        control.deleteUnavailableNodes(name);
//        return Response.ok().build();
//    }


}
