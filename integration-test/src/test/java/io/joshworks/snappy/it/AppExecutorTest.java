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

import io.joshworks.restclient.http.Unirest;
import io.joshworks.snappy.executor.AppExecutors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.joshworks.snappy.SnappyServer.*;
import static io.joshworks.snappy.parser.MediaTypes.produces;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by josh on 3/14/17.
 */
public class AppExecutorTest {


    private static final String EXECUTOR_A = "EXECUTOR-A";
    private static final String RETURN_VALUE = "DONE";
    private static CountDownLatch executorLatch = new CountDownLatch(1);
    private static CountDownLatch customExecutorLatch = new CountDownLatch(1);
    private static CountDownLatch schedulerLatch = new CountDownLatch(1);

    @BeforeClass
    public static void setup() {
        executor(EXECUTOR_A, 1, 1, 200);

        get("/executor", (exchange -> AppExecutors.submit(() -> executorLatch.countDown())));
        get("/custom-executor", (exchange -> AppExecutors.submit(EXECUTOR_A, () -> customExecutorLatch.countDown())));
        get("/scheduler", exchange -> {
            try {
                ScheduledFuture<String> schedule = AppExecutors.schedule(() -> RETURN_VALUE, 0, TimeUnit.SECONDS);
                schedulerLatch.countDown();
                exchange.send(schedule.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, produces("txt"));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
    }

    @Test
    public void defaultExecutor() throws Exception {
        int status = Unirest.get("http://localhost:9000/executor").asString().getStatus();
        assertEquals(200, status);
        if (!executorLatch.await(10, TimeUnit.SECONDS)) {
            fail("Task didn't finish");
        }
    }

    @Test
    public void defaultScheduler() throws Exception {
        int status = Unirest.get("http://localhost:9000/scheduler").asString().getStatus();
        assertEquals(200, status);
        if (!schedulerLatch.await(10, TimeUnit.SECONDS)) {
            fail("Task didn't finish");
        }
    }

    @Test
    public void customExecutor() throws Exception {
        int status = Unirest.get("http://localhost:9000/custom-executor").asString().getStatus();
        assertEquals(200, status);

        if (!customExecutorLatch.await(10, TimeUnit.SECONDS)) {
            fail("Task didn't finish");
        }
    }
}
