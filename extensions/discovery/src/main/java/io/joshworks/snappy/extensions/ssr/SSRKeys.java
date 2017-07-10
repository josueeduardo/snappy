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

package io.joshworks.snappy.extensions.ssr;

/**
 * Created by Josh Gontijo on 3/29/17.
 */
public class SSRKeys {

    public static final String SSR_LOGGER = "SSR";

    public static final String PROPERTY_PREFIX = "ssr.";
    public static final String CLIENT_PREFIX = PROPERTY_PREFIX + "client.";
    public static final String REGISTRY_PREFIX = PROPERTY_PREFIX + "registry.";

    //This is simply an identifier of the container port
    //It is used ONLY IF no ssr.registry.port is used
    //Use case for this is OUTSIDE Docker for example, where there's no need to use the ssr.registry.port
    //Since snappy will provide the port
//    public static final String SNAPPY_PORT = "snappy.port";

    //ssr discovery registry
    //ssr registry properties
    public static final String SSR_REGISTRY_HOST = REGISTRY_PREFIX + "host";
    public static final String SSR_REGISTRY_PORT = REGISTRY_PREFIX + "port";
    //ssr client properties
    public static final String SSR_CLIENT_ENABLED = CLIENT_PREFIX + "fetchServices";
    public static final String SSR_CLIENT_APP_NAME = CLIENT_PREFIX + "name";
    public static final String SSR_CLIENT_DISCOVERABLE = CLIENT_PREFIX + "discoverable";
    public static final String SSR_CLIENT_HOST = CLIENT_PREFIX + "host";
    public static final String SSR_CLIENT_PORT = CLIENT_PREFIX + "port";
    public static final String SSR_CLIENT_ON_AWS = CLIENT_PREFIX + "onAws";
    public static final String SSR_CLIENT_USE_HOSTNAME = CLIENT_PREFIX + "useHostname";

}
