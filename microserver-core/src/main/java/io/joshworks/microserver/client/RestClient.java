package io.joshworks.microserver.client;

import io.joshworks.microserver.parser.Parsers;
import io.undertow.util.Methods;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.core.Headers;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by josh on 3/11/17.
 */
public class RestClient {

    private final ResteasyClient client;
    private String mediaType = MediaType.APPLICATION_JSON;
    private MultivaluedMap<String, Object> headers = new Headers<>();
    private List<String> accepts = new ArrayList<>();

    RestClient(ResteasyClient client) {
        this.client = client;
    }

    public Client client() {
        return client;
    }

    public <T> T get(String url, Class<T> type) {
        return invocation(url).get(type);
    }

    public Response get(String url) {

        return invocation(url).get();
    }

    public <T> void getAsync(String url, Consumer<T> success) {
        this.getAsync(url, success, (T) -> {
        });
    }

    public <T> void getAsync(String url, Consumer<T> success, Consumer<Throwable> error) {
        invocation(url).async().get(new ResponseCallback<>(success, error));
    }

    public Future<Response> getAsync(String url) {
        return invocation(url).async().get();
    }

    //------- POST -----

    public <T> Response post(String url, T body) {
        return method(Methods.POST_STRING, url, body);
    }

    public <T, R> R post(String url, T body, Class<R> responseType) {
        InputStream inputStream = post(url, body).readEntity(InputStream.class);
        return Parsers.find(accepts).read(inputStream, responseType);
    }

    public <T> Future<Response> postAsync(String url, T body) {
        return methodAsync(Methods.POST_STRING, url, body);
    }

    public <T, R> void postAsync(String url, T body, Consumer<R> success) {
        methodAsync(Methods.POST_STRING, url, body, success);
    }

    public <T, R> void postAsync(String url, T body, Consumer<R> success, Consumer<Throwable> error) {
        methodAsync(Methods.POST_STRING, url, body, success, error);
    }

    //------- PUT -----

    public <T> Response put(String url, T body) {
        return method(Methods.PUT_STRING, url, body);
    }

    public <T, R> R put(String url, T body, Class<R> responseType) {
        return post(url, body).readEntity(responseType);
    }

    public <T> Future<Response> putAsync(String url, T body) {
        return methodAsync(Methods.PUT_STRING, url, body);
    }

    public <T, R> void putAsync(String url, T body, Consumer<R> success) {
        methodAsync(Methods.PUT_STRING, url, body, success);
    }

    public <T, R> void putAsync(String url, T body, Consumer<R> success, Consumer<Throwable> error) {
        methodAsync(Methods.PUT_STRING, url, body, success, error);
    }

    //------- DELETE -----

    public <T> Response delete(String url, T body) {
        return method(Methods.DELETE_STRING, url, body);
    }

    public <T, R> R delete(String url, T body, Class<R> responseType) {
        return post(url, body).readEntity(responseType);
    }

    public <T> Future<Response> deleteAsync(String url, T body) {
        return methodAsync(Methods.DELETE_STRING, url, body);
    }

    public <T, R> void deleteAsync(String url, T body, Consumer<R> success) {
        methodAsync(Methods.DELETE_STRING, url, body, success);
    }

    public <T, R> void deleteAsync(String url, T body, Consumer<R> success, Consumer<Throwable> error) {
        methodAsync(Methods.DELETE_STRING, url, body, success, error);
    }

    //------- METHODS -----

    private  <T> Response method(String method, String url, T body) {
        String bodyString = Parsers.getParser(mediaType).write(body);
        return invocation(url).method(method, Entity.entity(bodyString, mediaType));
    }

    private <T, R> R method(String method, String url, T body, Class<R> responseType) {
        return method(method, url, body).readEntity(responseType);
    }

    private <T> Future<Response> methodAsync(String method, String url, T body) {
        String bodyString = Parsers.getParser(mediaType).write(body);
        return invocation(url).async().method(method, Entity.entity(bodyString, mediaType));
    }

    private <T, R> void methodAsync(String method, String url, T body, Consumer<R> success) {
        methodAsync(method, url, body, success, (e) -> {
        });
    }

    private <T, R> void methodAsync(String method, String url, T body, Consumer<R> success, Consumer<Throwable> error) {
        String bodyString = Parsers.getParser(mediaType).write(body);
        invocation(url).async().method(method, Entity.entity(bodyString, mediaType), new ResponseCallback<>(success, error));
    }


    public RestClient mediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public RestClient header(String key, Object val) {
        headers.add(key, val);
        return this;
    }

    public RestClient header(String key, List<Object> vals) {
        headers.put(key, vals);
        return this;
    }

    public RestClient accepts(String... types) {
        accepts.addAll(Arrays.asList(types));
        return this;
    }

    private Invocation.Builder invocation(String url) {
        return client.target(url)
                .request(mediaType)
                .headers(headers)
                .accept(accepts.toArray(new String[accepts.size()]));
    }

    private static class ResponseCallback<T> implements InvocationCallback<T> {

        private final Consumer<T> success;
        private final Consumer<Throwable> error;

        ResponseCallback(Consumer<T> success, Consumer<Throwable> error) {
            this.success = success;
            this.error = error;
        }

        @Override
        public void completed(T t) {
            success.accept(t);
        }

        @Override
        public void failed(Throwable throwable) {
            error.accept(throwable);
        }
    }

}
