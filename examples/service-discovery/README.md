### Running with `java -jar` ###

```bash
    mvn clean install
    java -jar registry-server/target/registry-server.jar
    java -jar hello-service/target/hello-service.jar
    java -jar world-service/target/world-service.jar

```

### Running with Docker compose ###

```bash
    mvn clean install
    docker-compose build
    docker-compose up
    
```