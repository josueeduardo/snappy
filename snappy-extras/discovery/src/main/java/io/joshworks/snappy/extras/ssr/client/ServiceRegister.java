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
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoFuture;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josue on 16/06/2016.
 */
public class ServiceRegister implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    public static final String SSR_ENDPOINT = "/ssr";
    private static final Object LOCK = new Object();
    private static final AtomicInteger retryCounter = new AtomicInteger();
    private static final int RETRY_INTERVAL = 10;//in seconds
    static boolean shutdownSignal = false;
    private WebSocketChannel webSocketChannel;
    private final ServiceStore store;
    private final Instance instance;
    private final String serverUrl;
    private final ScheduledExecutorService executorService;

    public ServiceRegister(ServiceStore store, Instance instance, String serverUrl) {
        this.store = store;
        this.instance = instance;
        this.serverUrl = serverUrl;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public ServiceRegister(ServiceStore store, Instance instance, String serverUrl, ScheduledExecutorService executorService) {
        this.store = store;
        this.instance = instance;
        this.serverUrl = serverUrl;
        this.executorService = executorService;
    }

    void init() {
        synchronized (LOCK) {
            logger.info("##############################################");
            logger.info("##### BOOTSTRAPING SSR SERVICE DISCOVERY #####");
            logger.info("##############################################");
            logger.info("Application name: {}", instance.getName());
            logger.info("Registry URL: {}", serverUrl);

            register();
        }
    }

    public void register() {
        deregister();

        synchronized (LOCK) {
            if (!shutdownSignal && (webSocketChannel == null || !webSocketChannel.isOpen())) {
                retryCounter.set(0);
                executorService.schedule(this, 5, TimeUnit.SECONDS);
            }
        }
    }

    public void shutdown() {
        synchronized (LOCK) {
            logger.info(":: Shutting down ::");
            shutdownSignal = true;
        }
        deregister();
    }

    public void deregister() {
        synchronized (LOCK) {
            try {
                if (webSocketChannel != null && webSocketChannel.isOpen()) {
                    logger.info("SSR: Closing WS webSocketChannel");

                    webSocketChannel.setCloseCode(1000); //normal closure
                    webSocketChannel.setCloseReason("Service disconnected");
                    webSocketChannel.sendClose();
                }
            } catch (IOException e) {
                logger.error("SSR: Error while closing the webSocketChannel", e);
            }
        }
    }

    @Override
    public void run() {
        synchronized (LOCK) {
            try {
                //TODO change to connect - send data
                //TODO remove path parameter,it should be sent after connect
                String registryUrl = "ws://" + serverUrl + SSR_ENDPOINT + "/" + instance.getName();

                //TODO implement undertow client websocket (DONE?)
                //TODO implement rotating servers (multiple servers)

                logger.info("SSR: Trying to connect to {}", registryUrl);
                //TODO move to constructor
                XnioWorker worker = Xnio.getInstance().createWorker(OptionMap.builder()
                        .set(Options.WORKER_IO_THREADS, 2)
                        .set(Options.CONNECTION_HIGH_WATER, 1000000)
                        .set(Options.CONNECTION_LOW_WATER, 1000000)
                        .set(Options.WORKER_TASK_CORE_THREADS, 1)
                        .set(Options.WORKER_TASK_MAX_THREADS, 1)
                        .set(Options.TCP_NODELAY, true)
                        .set(Options.CORK, true)
                        .getMap());

                //ref: https://blog.sagaoftherealms.net/?p=607
                IoFuture<WebSocketChannel> connectionFuture = new WebSocketClient.ConnectionBuilder(
                        worker,
                        new DefaultByteBufferPool(false, 2048),
                        new URI(registryUrl)).connect();

                //block until connects
                webSocketChannel = connectionFuture.get();
                webSocketChannel.getReceiveSetter().set(new ServiceClientEndpoint(this, store, instance));


                logger.info("Connected to {}", registryUrl);

            } catch (Exception e) {
                logger.error("Could not connect to the registry, retrying in {}s", RETRY_INTERVAL);
                executorService.schedule(this, RETRY_INTERVAL, TimeUnit.SECONDS);
            }
        }
    }


//    @Override
//    public void run() {
//        synchronized (LOCK) {
//            try {
//                String registryUrl =
//                        "ws://" +
//                                Configuration.getRegistryUrl() + SSR_ENDPOINT +
//                                "/" + Configuration.getCurrentInstance().getName();
//
//                //TODO implement undertow client websocket
//
//                IoFuture<WebSocketChannel> connect = new WebSocketClient.ConnectionBuilder().connect();
//                WebSocketChannel webSocketChannel = connect.get();
//                boolean open = webSocketChannel.isOpen();
//
//
//                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//
//                logger.log(":: Trying to connect to {0} ::", new Object[]{registryUrl, retryCounter.incrementAndGet()});
//
//                ServiceClientEndpoint endpoint = new ServiceClientEndpoint(this, store);
//
//                this.webSocketChannel = container.connectToServer(endpoint, new URI(registryUrl));
//
//                logger.log(":: Connected ! ::", this.webSocketChannel.getId());
//
//            } catch (Exception e) {
//                logger.log(Level.WARNING, ":: Could not connect to the registry, retrying in {0}s ::", RETRY_INTERVAL);
//                executorService.schedule(this, RETRY_INTERVAL, TimeUnit.SECONDS);
//            }
//        }
//    }
}