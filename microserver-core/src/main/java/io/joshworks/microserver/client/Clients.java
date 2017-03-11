package io.joshworks.microserver.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

/**
 * Created by josh on 3/11/17.
 */
public class Clients {

    private final ResteasyClient client;

    private static Clients instance = new Clients();

    private Clients() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build();
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpClient);
        client = new ResteasyClientBuilder()
                .httpEngine(engine)
                .connectionPoolSize(20)
                .build();
    }

    public static RestClient client() {
        return new RestClient(instance.client);
    }


}
