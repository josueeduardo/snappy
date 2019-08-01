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


import io.joshworks.snappy.http.Request;
import io.joshworks.snappy.http.Response;

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

    public Response getServices(Request request) {
        Set<Service> services = control.getServices();
        return Response.withBody(services);
    }


    public Response getService(Request exchange) {
        Service service = control.getService(exchange.pathParameter("name"));
        return Response.withBody(service);
    }

    public Response addLink(Request request)  {
        String source = request.pathParameter("name");
        String target = request.body().asJson().getObject().getString("target");

        control.addLink(source, target);
        return Response.withStatus(204);
    }

//    @PUT
//    @Path("{name}")
//    public Response deleteUnavailableNodes(@PathParam("name") String name) throws Exception {
//        control.deleteUnavailableNodes(name);
//        return Response.ok().build();
//    }


}
