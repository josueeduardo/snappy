package examples;

import com.josue.simpletow.Config;
import com.josue.simpletow.Microserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by josh on 3/5/17.
 */
public class Main {


    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        Config config = new Config()
                        .coreThreads(10)
                        .ioThreads(5)
                        .maxThreads(20);



        Microserver microserver = new Microserver(config);
//        microserver.get("/hello/{id}", exchange -> {});
        microserver.get("/hello/{name}", exchange -> {
            logger.info(Thread.currentThread().getName());
            exchange.send(new User(exchange.pathParameter("name")));
//            AppExecutors.executor().submit(() -> logger.info(Thread.currentThread().getPoolName()));
//            AppExecutors.scheduler().schedule(() -> logger.info("SCHEDULED -> " + Thread.currentThread().getPoolName()), 2, TimeUnit.SECONDS);
        });

        microserver.post("/echo", exchange -> exchange.send(exchange.body(User.class)));

        microserver.websocket("/ws/{id}", new SampleEndpoint());
        microserver.staticFiles("/pages");

        microserver.start();
    }

    public static class User {
        public String name;

        public User(String name) {
            this.name = name;
        }
    }
}
