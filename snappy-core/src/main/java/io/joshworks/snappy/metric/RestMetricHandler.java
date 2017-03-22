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

package io.joshworks.snappy.metric;

import io.undertow.server.HttpHandler;

/**
 * Created by josh on 3/9/17.
 */
public class RestMetricHandler extends MetricsHandler {

    private final String url;
    private final String method;

    public RestMetricHandler(String method, String url, HttpHandler next) {
        super(next);
        this.url = url;
        this.method = method;
    }

    RestMetrics getRestMetrics() {
        return new RestMetrics(url, method, getMetrics());
    }

    public static class RestMetrics {
        private final String url;
        private final String method;
        private final MetricResult metrics;

        RestMetrics(String url, String method, MetricResult metrics) {
            this.url = url;
            this.method = method;
            this.metrics = metrics;
        }

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public MetricResult getMetrics() {
            return metrics;
        }
    }

}
