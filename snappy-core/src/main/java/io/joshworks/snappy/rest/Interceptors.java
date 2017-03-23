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

package io.joshworks.snappy.rest;

import io.undertow.util.Methods;

/**
 * Created by Josh Gontijo on 3/23/17.
 */
public class Interceptors {

    public static Interceptor cors() {
        return new Interceptor(Interceptor.Type.BEFORE, "/*", (exchange -> {
            exchange.header("Access-Control-Allow-Origin", "*");
            exchange.header("Access-Control-Allow-Credentials", "true");
            exchange.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            exchange.header("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Authorization, Access-Control-Request-Method, Access-Control-Request-Headers");
            if(exchange.method().equalsIgnoreCase(Methods.OPTIONS_STRING)){
                exchange.status(200).end();
            }
        }));
    }

}
