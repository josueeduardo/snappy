package io.joshworks.snappy.client;

import com.google.gson.Gson;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

/**
 * Created by josh on 3/11/17.
 */
public class RestClient {

    private static final Gson gson = new Gson();

    static {
        Unirest.setObjectMapper(new ObjectMapper() {
            @Override
            public <T> T readValue(String value, Class<T> valueType) {
                return gson.fromJson(value, valueType);
            }

            @Override
            public String writeValue(Object value) {
                return gson.toJson(value);
            }
        });
    }

    private RestClient() {

    }

    public static GetRequest get(String url) {
        return Unirest.get(url);
    }

    public static HttpRequestWithBody post(String url) {
        return Unirest.post(url);
    }

    public static HttpRequestWithBody put(String url) {
        return Unirest.put(url);
    }

    public static HttpRequestWithBody delete(String url) {
        return Unirest.delete(url);
    }

    public static HttpRequestWithBody options(String url) {
        return Unirest.options(url);
    }

    public static GetRequest head(String url) {
        return Unirest.head(url);
    }

    public static HttpRequestWithBody patch(String url) {
        return Unirest.patch(url);
    }

    public static void shutdown() {
        try {
            Unirest.shutdown();
        } catch (Exception e) {

        }
    }

}
