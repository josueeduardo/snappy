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

package io.joshworks.snappy.it;

import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.SimpleClient;
import io.joshworks.snappy.metric.MetricData;
import io.joshworks.snappy.metric.RestMetricHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class AdminTest {

    @BeforeClass
    public static void setup() {
        enableHttpMetrics();

        get("/test", exchange -> {
        });


        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void metrics() throws Exception {
        int status = SimpleClient.get("http://localhost:9000/test").asString().getStatus();
        assertEquals(200, status);

        HttpResponse<MetricData> response = SimpleClient.get("http://localhost:9100/metrics").asObject(MetricData.class);
        assertEquals(200, response.getStatus());

        MetricData metrics = response.getBody();
        assertNotNull(metrics);
        assertEquals(1, metrics.getResources().size());

        Optional<RestMetricHandler.RestMetrics> foundMetrics = metrics.getResources().stream()
                .filter(m -> m.getUrl().equals("/test"))
                .findFirst();

        assertTrue(foundMetrics.isPresent());
        RestMetricHandler.RestMetrics metric = foundMetrics.get();
        assertEquals(1L, metric.getMetrics().getTotalRequests());
        assertEquals(1, metric.getMetrics().getResponses().size());
        assertEquals(1, metric.getMetrics().getResponses().get("200").get()); //200 OK

    }

    @Test
    public void pageExists() throws Exception {
        HttpResponse<String> response = SimpleClient.get("http://localhost:9100/").asString();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void contentType() throws Exception {
        HttpResponse<String> response = SimpleClient.get("http://localhost:9100/").asString();
        assertEquals("text/html", response.getHeaders().getFirst(io.undertow.util.Headers.CONTENT_TYPE_STRING));
    }

}
