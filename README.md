# Snappy #
A tiny and powerful web framework for Java 8

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
- Rest client
- Multipart support
- Executors and schedulers managed by the server
- Extensible
- Maven uber jar plugin using [Spring boot](https://projects.spring.io/spring-boot/)


### Maven setup ###

```xml
    <dependency>
        <groupId>io.joshworks.snappy</groupId>
        <artifactId>snappy</artifactId>
        <version>0.3.0</version>
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
            Files.copy(theFile, someOutputStream);
        });
    }
}
```

### Rest client ###
```java
public class App {

    public static void main(final String[] args) {
        //wrapped Unirest api
           String page = SimpleClient.get("http://www.google.com").asString();
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

## Extensions ##
Extension allows access to internal API, to add or modify the server behaviour.

```java
public class App {

     public static void main(String[] args) {
           
            
     }
}
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
    
    register(SnappyExtension extension)
    
    enableTracer()
    cors()
    
    executor(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis)
    scheduler(String name, int corePoolSize, long keepAliveMillis)
    
    
    
```

