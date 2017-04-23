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

package io.joshworks.snappy.extensions.ssr.server.sse;

import io.joshworks.snappy.client.sse.EventData;
import io.joshworks.snappy.sse.SseBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.joshworks.snappy.extensions.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 4/2/17.
 */
public class Hearbeat {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);


    private ScheduledExecutorService scheduler;
    private int period;
    private ScheduledFuture<?> scheduledFuture;

    public Hearbeat(ScheduledExecutorService scheduler, int period) {
        this.scheduler = scheduler;
        this.period = period;
    }

    public void start() {
        logger.info("Starting hearbeat monitor");
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            logger.debug("Sending ACK event, ID: {}", now);
            SseBroadcaster.broadcast(new EventData("", String.valueOf(now), EventType.ACK.name()));

        }, period, period, TimeUnit.SECONDS);
    }

    public void stop() {
        if (scheduledFuture != null) {
            logger.info("Stopping hearbeat monitor");
            scheduledFuture.cancel(false);
            scheduledFuture = null;
        }
    }

}
