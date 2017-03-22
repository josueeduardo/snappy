# Snappy #
A tiny but powerful server for Java 8

Features:
    
- Based on [Undertow](http://undertow.io) handlers
- Rest with content negotiation (media type only)
- Server sent events
- Websockets
- Built in service discovery (in progress)
- Small. Less than 6mb
- No magic, plain and simple
- Static files
- Rest client using [Unirest](https://github.com/Mashape/unirest-java)
- Multipart support
- Executors and schedulers managed by the server
- Metrics: total requests, responses codes per endpoint, thread and memory usage, and user provided metrics.
- Maven uber jar plugin using [Spring boot](https://projects.spring.io/spring-boot/)


## Usage ##

#### ** Available on Maven central soon ####


### Import ###
```java
import static io.joshworks.snappy.SnappyServer.*;
```

#### Hello ####
```java
public class App {

    public static void main(final String[] args) {
       get("/hello", (exchange) -> exchange.send("Hello !", "txt")); //or text/plain
       start(); //8000
    }
}
```

#### Path parameter ####
```java
public class App {

    public static void main(final String[] args) {
       get("/hello/{name}", (exchange) -> {
           String name = exchange.pathParameter("name");
           exchange.send("Hello " + name, "txt");
       });
       
       start();
    }
}
```

#### Receiving JSON ####
```java
public class App {

    public static void main(final String[] args) {
       post("/users", (exchange) -> {

           User user = exchange.body().asObject(User.class);

       });
       
       start();
    }
}
```

#### Filter ####
```java
public class App {

    public static void main(final String[] args) {
       
       before("/*", (exchange) -> {/*...*/}); 
       after("/users", (exchange) -> {/*...*/}); 
       
       get("/users", (exchange) -> {/*...*/});

       
       start();
    }
}
```

#### Error handling ####
```java
public class App {

    public static void main(final String[] args) {
       
       exception(Exception.class, (e, exchange) -> exchange.status(500).send(e.getMessage()));
       
       get("/users", (exchange) -> {/*...*/});

       
       start();
    }
}
```


#### Sending JSON ####
```java
public class App {

    public static void main(final String[] args) {
       get("/users", (exchange) -> exchange.send(new User("Yolo")));
       start();
    }
}
```
#### Sending JSON ####
```java
public class App {

    public static void main(final String[] args) {
       get("/users", (exchange) -> exchange.send(new User("Yolo")));
       start();
    }
}
```


#### Static files ####
```java
public class App {

    public static void main(final String[] args) {
        //server files from src/main/resources/static
        //available on /pages
        staticFiles("/pages");
        
//        staticFiles("/pages", "someFolder"); // src/main/resources/someFolder
    }
}
```

#### Uploading file ####
```java
public class App {

    public static void main(final String[] args) {
        multipart("/fileUpload", (exchange) -> {
            Path theFile = exchange.part("theFile").getPath();
            Files.copy(theFile, someOutputStrem);
        });
    }
}
```

#### Rest client ####
```java
public class App {

    public static void main(final String[] args) {
        //wrapped Unirest api
           String page = RestClient.get("http://www.google.com").asString();
    }
}
```

#### Broadcasting data with SSE ####
```java
public class App {

    public static void main(final String[] args) {
       sse("/event-stream");
       start();
       
       //.... somewhere else
       SSEBroadcaster.broadcast("some data");
    }
}
```

#### Registering a parser ####
```java
public class App {

    public static void main(final String[] args) {
       Parsers.register(myXmlParser);
       
       get("/xml", (exchange) -> exchange.send("some-xml"), produces("xml"));
       
    }
}
```
#### Resource group ####
```java
public class App {

     public static void main(String[] args) {
            group("/groupA", () -> {
                get("/a", (exchange) -> {/* ... */});
                put("/b", (exchange) -> {/* ... */});
    
                group("/subgroup", () -> {
                    get("/c", (exchange) -> {/* ... */});
                });
            });
    
            group("/groupB", () -> {
                get("/d", (exchange) -> {/* ... */});
            });
            
        }
}
```

```java

    //Server core methods
    start();
    stop();
    
    port(int port);
    address(String address);
    tcpNoDeplay(boolean tcpNoDelay);
    
    adminPort(int port);
    adminAddress(String address);
    
    ioThreads(int ioThreads);
    workerThreads(int core, int max);
    
    enableTracer();
    enableHttpMetrics();
    
    executor(String name, int corePoolSize, int maxPoolSize, long keepAliveMillis);
    scheduler(String name, int corePoolSize, long keepAliveMillis);
    
    
    
```


##### Metrics available at /metrics on port 9090 (default admin port) ####

