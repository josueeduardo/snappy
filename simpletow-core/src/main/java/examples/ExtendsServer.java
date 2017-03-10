package examples;

import com.josue.simpletow.Microserver;

/**
 * Created by josh on 3/8/17.
 */
public class ExtendsServer extends Microserver {

    public static void main(String[] args) {
        Microserver server = new ExtendsServer();
        server.get("/hello/{name}", exchange -> exchange.send("Hello " + exchange.queryParameter("name")));
        server.start();
    }

}
