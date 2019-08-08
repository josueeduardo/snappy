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
        <version>0.5.2</version>
    </dependency>
    
```

### Import ###
```java
import static io.joshworks.snappy.SnappyServer.*;
import static io.joshworks.snappy.http.Response.*;
```

### Hello ###
```java
public class App {

    public static void main(final String[] args) {
       get("/hello", req -> ok("Hello !", "txt")); //or text/plain
       start(); //9000
    }
}
```

### Path parameter ###
```java
public class App {

    public static void main(final String[] args) {
       get("/hello/{name}", req -> ok("Hello " + req.pathParameter("name"), "txt"));
       
       start();
    }
}
```

### Receiving JSON ###
```java
public class App {

    public static void main(final String[] args) {
       post("/users", req -> {
           User user = req.body().asObject(User.class);
           //..
           return ok();
       });
       
       start();
    }
}
```

### Sending JSON ###
```java
public class App {

    public static void main(final String[] args) {
       get("/users", req -> ok(new User("John Doe")));
       start();
    }
}
```

### Filter ###
```java
public class App {

    public static void main(final String[] args) {
        
       beforeAll("/users", req -> System.out.println("A")); 
       before("/users", req -> System.out.println("B")); 
       after("/users", (req, res) -> System.out.println("C")); 
       afterAll("/users", (req, res) -> System.out.println("D")); 
       
       get("/users", req -> {
           System.out.println("Users");
            return ok();
       });
               
       start();
        
        // Prints:
        //A
        //B
        //Users
        //C
        //D
    }
}
```

### Error handling ###
```java
public class App {

    public static void main(final String[] args) {
       
       exception(Exception.class, (ex, req) -> Response.internalServerError(ex));
       
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
        post("/fileUpload", req -> {
            Path theFile = req.multiPartBody("theFile").file().path();
            Files.copy(theFile, someOutputStream);
        });
    }
}
```

### Broadcasting data with SSE ###
```java
public class ClockServer {

    public static void main(final String[] args) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            sse("/clock");
    
            onStart(() -> scheduler.scheduleAtFixedRate(() ->
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
       
       get("/xml", req -> ok("<root></root>"), produces("xml"));
       
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
            get("/a", req -> {/* ... */});
            put("/b", req -> {/* ... */});

            group("/subgroup", () -> {
                get("/c", req -> {/* ... */});
            });
        });

        // /groupB/d
        group("/groupB", () -> {
            get("/d", req -> {/* ... */});
        });
        
        }
}
```

### Uber jar ###
```xml
<build>
    <finalName>${project.artifactId}</finalName>
    <plugins>
        <plugin>
            <groupId>io.joshworks.snappy</groupId>
            <artifactId>snappy-maven-plugin</artifactId>
            <version>0.5.2</version>
        </plugin>
    </plugins>
</build>
```

