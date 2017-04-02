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

package io.joshworks.snappy.extras.ssr.client;


import io.joshworks.snappy.extras.ssr.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.joshworks.snappy.extras.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josue on 16/06/2016.
 */
public abstract class ServiceRegister implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);


    private static final Object LOCK = new Object();
    private static final AtomicInteger retryCounter = new AtomicInteger();
    private static final int RETRY_INTERVAL = 10;//in seconds
    private final ScheduledExecutorService executorService;
    private boolean shutdownSignal = false;

    protected final ServiceStore store;
    protected final Instance instance;
    protected final String registryUrl;

    public ServiceRegister(ServiceStore store, Instance instance, String registryUrl, ScheduledExecutorService executorService) {
        this.store = store;
        this.instance = instance;
        this.registryUrl = registryUrl;
        this.executorService = executorService;
    }

    protected abstract void connect() throws Exception;

    protected abstract void disconnect();

    void bootstrap() {
        synchronized (LOCK) {
            logger.info("Bootstrap service discovery");
            logger.info("Application name: {}", instance.getName());
            logger.info("Registry URL: {}", registryUrl);

            register();
        }
    }

    public void register() {
        synchronized (LOCK) {
            if (!shutdownSignal) {
                retryCounter.set(0);
                executorService.schedule(this, 5, TimeUnit.SECONDS);
            }
        }
    }

    public void shutdown() {
        synchronized (LOCK) {
            logger.info("Shutting down...");
            shutdownSignal = true;
            disconnect();
        }
    }

    public boolean shutdownRequested() {
        return shutdownSignal;
    }

    @Override
    public void run() {
        synchronized (LOCK) {
            try {
                //TODO change to connect - send data
                //TODO remove path parameter,it should be sent after connect

                if (!shutdownSignal) {
                    connect();
                }

                logger.info("Connected to {}", registryUrl);

            } catch (Exception e) {
                logger.error("Could not connect to the registry [{}], retrying in {}s, error message: {}", registryUrl, RETRY_INTERVAL, e.getMessage());
                executorService.schedule(this, RETRY_INTERVAL, TimeUnit.SECONDS);
            }
        }
    }

}
