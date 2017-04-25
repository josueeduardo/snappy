# Snappy #
A tiny and powerful server for Java 8

Features:
    
- Based on [Undertow](http://undertow.io) handlers
- Rest with content negotiation (media type only)
- Server sent events
- Server sent events client
- Websockets
- Websocket client
- Built in simple service discovery
- Small. ~6mb
- No magic, plain and simple
- Small memory footprint
- Static files
- Rest client using [Unirest](https://github.com/Mashape/unirest-java)
- Multipart support
- Executors and schedulers managed by the server
- Extensible
- Metrics: total requests, responses codes per endpoint, thread and memory usage, and user provided metrics.
- Maven uber jar plugin using [Spring boot](https://projects.spring.io/spring-boot/)


### Installing ###

```xml
    <dependency>
        <groupId>io.joshworks.snappy</groupId>
        <artifactId>snappy</artifactId>
        <version>0.3</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>1.7.25</version>
    </dependency>

```

### Import ###
```java
import static io.joshworks.snappy.SnappyServer.*;
```

### Hello ###
```java
public class App {

    public static void main(final String[] args) {
       get("/hello", exchange -> exchange.send("Hello !", "txt")); //or text/plain
       start(); //9000
    }
}
```

### Path parameter ###
```java
public class App {

    public static void main(final String[] args) {
       get("/hello/{name}", exchange -> {
           String name = exchange.pathParameter("name");
           exchange.send("Hello " + name, "txt");
       });
       
       start();
    }
}
```

### Receiving JSON ###
```java
public class App {

    public static void main(final String[] args) {
       post("/users", exchange -> {

           User user = exchange.body().asObject(User.class);

       });
       
       start();
    }
}
```

### Sending JSON ###
```java
public class App {

    public static void main(final String[] args) {
       get("/users", exchange -> exchange.send(new User("Yolo")));
       start();
    }
}
```

### Filter ###
```java
public class App {

    public static void main(final String[] args) {
       
       before("/*", exchange -> System.out.println("Before All")); 
       after("/users", exchange -> System.out.println("After users")); 
       
       get("/users", exchange -> {
           //...
       });

       
       start();
    }
}
```

### Error handling ###
```java
public class App {

    public static void main(final String[] args) {
       
       exception(Exception.class, (error, exchange) -> exchange.status(500).send(error.exception.getMessage()));
       
       get("/users", exchange -> {
           throw new RuntimeException("Some error");
       });

       
       start();
    }
}
```

### Serving static files ###
```java
public class App {

    public static void main(final String[] args) {
        //from src/main/resources/static
        //available on /pages
        staticFiles("/pages");
        
//        staticFiles("/pages", "someFolder"); // src/main/resources/someFolder
    }
}
```

### Uploading file ###
```java
public class App {

    public static void main(final String[] args) {
        multipart("/fileUpload", exchange -> {
            Path theFile = exchange.part("theFile").file().path();
            Files.copy(theFile, someOutputStrem);
        });
    }
}
```

### Rest client ###
```java
public class App {

    public static void main(final String[] args) {
        //wrapped Unirest api
           String page = RestClient.get("http://www.google.com").asString();
    }
}
```

### Broadcasting data with SSE ###
```java
public class ClockServer {

    public static void main(final String[] args) {
            sse("/real-time");
    
            onStart(() -> AppExecutors.scheduleAtFixedRate(() ->
                    SseBroadcaster.broadcast(new Date().toString()), 1, 1, TimeUnit.SECONDS));
    
            start();
        }
}
```

### SSE groups / topics ###
```java
public class BroadcastEndpoint implements ServerSentEventConnectionCallback {

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        SseBroadcaster.addToGroup("my-group", connection);
    }
}

public class App {

    public static void main(final String[] args) {
       sse("/event-stream", new BroadcastEndpoint());
       start();
       
       //curl http://localhost:9000/event-stream
       
       //Send messages to all clients in 'my-group'
       SSEBroadcaster.broadcast("some data", "my-group");
    }
}
```

### Registering a parser ###
```java
public class App {

    public static void main(final String[] args) {
       Parsers.register(myXmlParser);
       
       get("/xml", exchange -> exchange.send("some-xml"), produces("xml"));
       
    }
}
```
### Resource group ###
```java
public class App {

     public static void main(String[] args) {
            
        // /groupA/a
        // /groupA/b
        // /groupA/subgroup/c
        group("/groupA", () -> {
            get("/a", exchange -> {/* ... */});
            put("/b", exchange -> {/* ... */});

            group("/subgroup", () -> {
                get("/c", exchange -> {/* ... */});
            });
        });

        // /groupB/d
        group("/groupB", () -> {
            get("/d", exchange -> {/* ... */});
        });
        
        }
}
```

### Executors ###
```java
public class App {

     public static void main(String[] args) {
            executors("my-thread-pool", 10, 20, 60000); //corePoolSize, maxPoolSize, keepAliveMillis
            schedulers("another-thread-pool", 10, 60000); //corePoolSize, keepAliveMillis
            
            //.... then
            
            AppExecutors.submit(myRunnable); //uses default
            AppExecutors.submit("my-thread-pool", myRunnable);
            
            AppExecutors.schedule(myCallable, 10, TimeUnit.SECONDS);
            AppExecutors.schedule("another-thread-pool", myCallable, 10, TimeUnit.SECONDS);
            
        }
}
```

### Uber jar ###
```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.joshworks</groupId>
            <artifactId>snappy-maven-plugin</artifactId>
            <version>0.2-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Server API ###
```java
 
    start()
    stop()
    
    port(int port)
    address(String address)
    portOffset(int offset)
    adminPort(int port)
    adminAddress(String address)
    tcpNoDeplay(boolean tcpNoDelay)
    
    onStart(Runnable task)
    onShutdown(Runnable task)
    
    ioThreads(int ioThreads)
    workerThreads(int core, int max)
    
    enableTracer()
    enableHttpMetrics()
    
    cors()
    
    executor(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis)
    scheduler(String name, int corePoolSize, long keepAliveMillis)
    
    
    
```

#### Metrics ###

```json
    //curl http://localhost:9100/metrics
    {
      "maxMemory": 3717201920,
      "totalMemory": 251658240,
      "freeMemory": 223553336,
      "usedMemory": 28104904,
      "resources": [ // enableHttpMetrics();
          {
            "url": "/users",
            "method": "GET",
            "metrics": {
              "metricsStartDate": "Apr 25, 2017 11:41:59 PM",
              "totalRequestTime": 91,
              "maxRequestTime": 18,
              "minRequestTime": 0,
              "totalRequests": 46,
              "responses": {
                "200": 30,
                "500": 16
              }
            }
          },
          {
            "url": "/users/{id}",
            "method": "GET",
            "metrics": {
              "metricsStartDate": "Apr 25, 2017 11:41:59 PM",
              "totalRequestTime": 80,
              "maxRequestTime": 16,
              "minRequestTime": 0,
              "totalRequests": 50,
              "responses": {
                "200": 46,
                "404": 4
            }
          }
        }
       ],
      "threadPools": [
        {
          "activeCount": 0,
          "completedTaskCount": 0,
          "corePoolSize": 0,
          "largestPoolSize": 0,
          "maximumPoolSize": 5,
          "poolSize": 0,
          "rejectionPolicy": "AbortPolicy",
          "taskCount": 0,
          "queueCapacity": 2147483647,
          "queuedTasks": 0,
          "poolName": "default-executor"
        },
        {
          "activeCount": 0,
          "completedTaskCount": 1670,
          "corePoolSize": 0,
          "largestPoolSize": 1,
          "maximumPoolSize": 5,
          "poolSize": 1,
          "rejectionPolicy": "AbortPolicy",
          "taskCount": 1671,
          "queueCapacity": -2147483648,
          "queuedTasks": 1,
          "poolName": "default-scheduler"
        }
      ],
      "appMetrics": {
         "my-metric": 123,
         "anotherOne": true,
         "andAnother": "Yolo"
      }
    }
    

```

