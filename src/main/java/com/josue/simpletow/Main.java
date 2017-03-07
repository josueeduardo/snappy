package com.josue.simpletow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by josh on 3/5/17.
 */
public class Main {


    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {

        Config config = new Config()
                        .coreThreads(10)
                        .ioThreads(5)
                        .maxThreads(20);


        Microserver microserver = new Microserver(config);
        microserver.get("/hello/{name}", exchange -> {
            logger.info(Thread.currentThread().getName());
            exchange.send(new User(exchange.pathParameter("name")));
            AppExecutor.executor().submit(() -> logger.info(Thread.currentThread().getName()));
            AppExecutor.scheduler().schedule(() -> logger.info("SCHEDULED -> " + Thread.currentThread().getName()), 2, TimeUnit.SECONDS);
        });

        microserver.post("/echo", exchange -> exchange.send(exchange.body(User.class)));

        microserver.start();
    }

    public static class User {
        public String name;

        public User(String name) {
            this.name = name;
        }
    }
}
