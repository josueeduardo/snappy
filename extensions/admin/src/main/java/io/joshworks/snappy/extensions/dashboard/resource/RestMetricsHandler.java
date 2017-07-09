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

package io.joshworks.snappy.extensions.dashboard.resource;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Handler that records some metrics
 *
 * @author Stuart Douglas
 * <p>
 * Modified by Josh Gontijo
 */
public class RestMetricsHandler implements HttpHandler {

    protected final HttpHandler next;
    private volatile MetricResult totalResult = new MetricResult(new Date());

    private boolean enabled = true;

    public RestMetricsHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (enabled) {
            next.handleRequest(exchange);
            final long start = System.currentTimeMillis();
            exchange.addExchangeCompleteListener((exchange1, nextListener) -> {
                long time = System.currentTimeMillis() - start;
                totalResult.update((int) time, String.valueOf(exchange1.getStatusCode()));
                nextListener.proceed();
            });
        }
        next.handleRequest(exchange);
    }

    public void reset() {
        this.totalResult = new MetricResult(new Date());
    }

    MetricResult getMetrics() {
        return enabled ? new MetricResult(this.totalResult) : new MetricResult();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static class MetricResult {

        private static final AtomicLongFieldUpdater<MetricResult> totalRequestTimeUpdater = AtomicLongFieldUpdater.newUpdater(MetricResult.class, "totalRequestTime");
        private static final AtomicIntegerFieldUpdater<MetricResult> maxRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(MetricResult.class, "maxRequestTime");
        private static final AtomicIntegerFieldUpdater<MetricResult> minRequestTimeUpdater = AtomicIntegerFieldUpdater.newUpdater(MetricResult.class, "minRequestTime");
        private static final AtomicLongFieldUpdater<MetricResult> invocationsUpdater = AtomicLongFieldUpdater.newUpdater(MetricResult.class, "totalRequests");

        private Date metricsStartDate;

        private volatile long totalRequestTime;
        private volatile int maxRequestTime;
        private volatile int minRequestTime = -1;
        private volatile long totalRequests;
        private ConcurrentHashMap<String, AtomicLong> responses = new ConcurrentHashMap<>();

        MetricResult(Date metricsStartDate) {
            this.metricsStartDate = metricsStartDate;
        }

        MetricResult() {

        }

        MetricResult(MetricResult copy) {
            this.metricsStartDate = copy.metricsStartDate;
            this.totalRequestTime = copy.totalRequestTime;
            this.maxRequestTime = copy.maxRequestTime;
            this.minRequestTime = copy.minRequestTime;
            this.totalRequests = copy.totalRequests;

            for (Map.Entry<String, AtomicLong> entry : copy.responses.entrySet()) {
                this.responses.put(entry.getKey(), new AtomicLong(entry.getValue().longValue()));
            }
        }

        void update(final int requestTime, String code) {
            totalRequestTimeUpdater.addAndGet(this, requestTime);
            int maxRequestTime;
            do {
                maxRequestTime = this.maxRequestTime;
                if (requestTime < maxRequestTime) {
                    break;
                }
            } while (!maxRequestTimeUpdater.compareAndSet(this, maxRequestTime, requestTime));

            int minRequestTime;
            do {
                minRequestTime = this.minRequestTime;
                if (requestTime > minRequestTime && minRequestTime != -1) {
                    break;
                }
            } while (!minRequestTimeUpdater.compareAndSet(this, minRequestTime, requestTime));
            invocationsUpdater.incrementAndGet(this);
            responses.putIfAbsent(code, new AtomicLong(0));
            responses.get(code).incrementAndGet();
        }
    }
}
