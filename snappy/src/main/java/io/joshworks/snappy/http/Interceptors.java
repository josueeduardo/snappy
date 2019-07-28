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

package io.joshworks.snappy.http;

import io.undertow.util.Methods;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Josh Gontijo on 3/23/17.
 */
public class Interceptors {

    private final List<RequestInterceptor> requestInterceptors = new LinkedList<>();
    private final List<ResponseInterceptor> responseInterceptors = new LinkedList<>();

    private final List<RequestInterceptor> rootRequestInterceptors = new LinkedList<>();
    private final List<ResponseInterceptor> rootResponseInterceptors = new LinkedList<>();

    public void add(RequestInterceptor interceptor) {
        requestInterceptors.add(interceptor);
    }

    public void add(ResponseInterceptor interceptor) {
        responseInterceptors.add(interceptor);
    }

    public void addRoot(RequestInterceptor interceptor) {
        rootRequestInterceptors.add(interceptor);
    }

    public void addRoot(ResponseInterceptor interceptor) {
        rootResponseInterceptors.add(interceptor);
    }


    public List<RequestInterceptor> requestInterceptors() {
        return requestInterceptors;
    }

    public List<ResponseInterceptor> responseInterceptors() {
        return responseInterceptors;
    }

    public List<RequestInterceptor> rootRequestInterceptors() {
        return rootRequestInterceptors;
    }

    public List<ResponseInterceptor> rootResponseInterceptors() {
        return rootResponseInterceptors;
    }

    public static RequestInterceptor cors() {
        return new RequestInterceptor("/*", (req) -> {
            if (req.method().equalsIgnoreCase(Methods.OPTIONS_STRING)) {
                Response response = Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Credentials", "true")
                        .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                        .header("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Authorization, Access-Control-Request-Method, Access-Control-Request-Headers");
                req.abortWith(response);
            }
        });
    }

}
