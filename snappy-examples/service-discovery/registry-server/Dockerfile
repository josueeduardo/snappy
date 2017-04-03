FROM anapsix/alpine-java:8
ADD target/registry-server.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]