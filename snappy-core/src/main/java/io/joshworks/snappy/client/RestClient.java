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

package io.joshworks.snappy.client;

import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.options.Options;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import io.joshworks.snappy.parser.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by josh on 3/11/17.
 */
public class RestClient {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);


    public static void init() {
        Options.refresh();
        JsonParser jsonParser = new JsonParser();
        Unirest.setObjectMapper(new ObjectMapper() {
            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                return jsonParser.readValue(value, valueType);
            }

            @Override
            public String writeValue(Object value) {
                return jsonParser.writeValue(value);
            }
        });
    }

    public static GetRequest get(String url) {
        return Unirest.get(url);
    }

    public static HttpRequestWithBody post(String url) {
        return Unirest.post(url);
    }

    public static HttpRequestWithBody put(String url) {
        return Unirest.put(url);
    }

    public static HttpRequestWithBody delete(String url) {
        return Unirest.delete(url);
    }

    public static HttpRequestWithBody options(String url) {
        return Unirest.options(url);
    }

    public static GetRequest head(String url) {
        return Unirest.head(url);
    }

    public static HttpRequestWithBody patch(String url) {
        return Unirest.patch(url);
    }

    public static void shutdown() {
        try {
            Unirest.shutdown();
        } catch (Exception e) {
            logger.warn("Error while closing rest client", e);
        }
    }

}
