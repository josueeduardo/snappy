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

package io.joshworks.examples.discovery.hello;

import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.extras.ssr.client.SSRClientExtension;

import static io.joshworks.snappy.SnappyServer.*;
import static io.joshworks.snappy.parser.MediaTypes.consumes;

/**
 * Created by Josh Gontijo on 4/2/17.
 */
public class HelloApp {

    public static void main(String[] args) {
        portOffset(1);
        register(new SSRClientExtension());

        get("/hello", exchange -> {

            String message = "Hello";
            String fromWorldService = RestClient.get("http://world-service/world").asString().getBody();
            exchange.send(message + " " + fromWorldService, "txt");

        }, consumes("txt"));

        start();
    }
}
