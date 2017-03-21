#Snappy
A tiny and powerful sever for Java 8

Features:
    
- Based on [Undertow](http://undertow.io) handlers
- Rest with content negotiation (media type only)
- Server sent events
- Websockets
- Bult in service discovery (in progress)
- Small. Less than 6mb
- Simple and compact
- Static files
- Rest client using [Unirest](https://github.com/Mashape/unirest-java)
- Multipart support
- Executors and schedulers managed by the server
- Metrics: total requests, responses codes per endpoint, thread and memory usage, and user provided metrics.
- Maven uber jar plugin using [Spring boot](https://projects.spring.io/spring-boot/)


##Simple usage

#### ** Available on Maven central soon


###Import
```java
import static io.joshworks.snappy.SnappyServer.*;
```

####Hello
```java
public class App {

    public static void main(final String[] args) {
       get("/hello", (exchange) -> exchange.send("Hello !", "txt")); //or text/plain
       start();
    }
}
```

####Path parameter
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

####Receiving JSON
```java
public class App {

    public static void main(final String[] args) {
       post("/users", (exchange) -> {

           User user = exchange.body().asObject(User.class);

       }, consumes("json")); //or application/json
       
       start();
    }
}
```

####Sending JSON
```java
public class App {

    public static void main(final String[] args) {
       get("/users", (exchange) -> exchange.send(new User("Yolo")));
       start();
    }
}
```

####Static files
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

####Uploading file
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

####Making request
```java
public class App {

    public static void main(final String[] args) {
        //wrapped Unirest api
           String page = RestClient.get("http://www.google.com").asString();
    }
}
```

####Broadcasting data with SSE
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

####Registering a parser
```java
public class App {

    public static void main(final String[] args) {
       Parsers.register(myXmlParser);
       
       get("/xml", (exchange) -> exchange.send("some-xml"), produces("xml"));
       
    }
}
```

##### Metrics available at /metrics on port 9090 (default admin port)

