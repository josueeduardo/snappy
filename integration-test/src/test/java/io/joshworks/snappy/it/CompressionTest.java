package io.joshworks.snappy.it;

import io.joshworks.restclient.http.Unirest;
import io.joshworks.snappy.it.util.Utils;
import io.undertow.util.Headers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.joshworks.snappy.SnappyServer.*;
import static io.joshworks.snappy.parser.MediaTypes.produces;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 7/6/17.
 */
public class CompressionTest {

    private static final String SERVER_URL = "http://localhost:9000";
    private static final String TEST_RESOURCE = "/gzip";

    private static final String dummyData = IntStream.range(0, 100000).boxed().map(i -> "A").collect(Collectors.joining("-"));

    @BeforeClass
    public static void setup() {

        enableTracer();
        get(TEST_RESOURCE, exchange -> exchange.send(dummyData, "txt"));

        get("/withProduces", exchange -> exchange.send(dummyData), produces("txt"));
        get("/emptyBody", exchange -> exchange.send(""), produces("txt"));
        post("/postConsumingBody", exchange -> exchange.send("with body"), produces("txt"));
        multipart("/multipart", exchange -> exchange.send("with body", "txt"));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
        Unirest.close();
    }

    //Content-Encoding is automatically removed by apache httpclient on decompressing
    //not using it here

    @Test
    public void gzipGet() throws Exception {
        InputStream inputStream = null;
        try {
            URL obj = new URL(SERVER_URL + TEST_RESOURCE);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestProperty(Headers.ACCEPT_ENCODING.toString(), "gzip");
            inputStream = conn.getInputStream();

            conn.connect();
            List<String> strings = conn.getHeaderFields().get(Headers.CONTENT_ENCODING.toString());
            assertTrue(strings != null && new HashSet<>(strings).contains("gzip"));
        } finally {
            Utils.closeStream(inputStream);
        }
    }

    @Test
    public void deflateGet() throws Exception {
        InputStream inputStream = null;
        try {
            URL obj = new URL(SERVER_URL + TEST_RESOURCE);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestProperty(Headers.ACCEPT_ENCODING.toString(), "deflate");
            inputStream = conn.getInputStream();

            conn.connect();
            List<String> strings = conn.getHeaderFields().get(Headers.CONTENT_ENCODING.toString());
            assertTrue(strings != null && new HashSet<>(strings).contains("deflate"));
        } finally {
            Utils.closeStream(inputStream);
        }
    }

    @Test
    public void acceptEncodingNone() throws Exception {
        InputStream inputStream = null;
        try {
            URL obj = new URL(SERVER_URL + TEST_RESOURCE);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            inputStream = conn.getInputStream();

            conn.connect();
            List<String> strings = conn.getHeaderFields().get(Headers.CONTENT_ENCODING.toString());
            assertTrue(strings == null || !new HashSet<>(strings).contains("gzip"));
        } finally {
            Utils.closeStream(inputStream);
        }
    }

    @Test
    public void emptyBody() throws Exception {
        InputStream inputStream = null;
        try {
            URL obj = new URL(SERVER_URL + "/emptyBody");
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestProperty(Headers.ACCEPT_ENCODING.toString(), "gzip");
            inputStream = conn.getInputStream();

            conn.connect();
            List<String> strings = conn.getHeaderFields().get(Headers.CONTENT_ENCODING.toString());
            assertTrue(strings == null || !new HashSet<>(strings).contains("gzip"));
        } finally {
            Utils.closeStream(inputStream);
        }
    }

    @Test
    public void withProduces() throws Exception {
        InputStream inputStream = null;
        try {
            URL obj = new URL(SERVER_URL + "/withProduces");
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestProperty(Headers.ACCEPT_ENCODING.toString(), "gzip");
            inputStream = conn.getInputStream();

            conn.connect();
            List<String> strings = conn.getHeaderFields().get(Headers.CONTENT_ENCODING.toString());
            assertTrue(strings != null && new HashSet<>(strings).contains("gzip"));
        } finally {
            Utils.closeStream(inputStream);
        }
    }

    @Test
    public void postConsumingBody() throws Exception {
        InputStream inputStream = null;
        try {
            URL obj = new URL(SERVER_URL + "/postConsumingBody");
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty(Headers.ACCEPT_ENCODING.toString(), "gzip");
            inputStream = conn.getInputStream();

            conn.connect();
            List<String> strings = conn.getHeaderFields().get(Headers.CONTENT_ENCODING.toString());
            assertTrue(strings != null && new HashSet<>(strings).contains("gzip"));
        } finally {
            Utils.closeStream(inputStream);
        }
    }

    @Test
    public void multipartGzip() throws Exception {
        InputStream inputStream = null;
        try {
            URL obj = new URL(SERVER_URL + "/multipart");
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty(Headers.ACCEPT_ENCODING.toString(), "gzip");
            inputStream = conn.getInputStream();

            conn.connect();
            List<String> strings = conn.getHeaderFields().get(Headers.CONTENT_ENCODING.toString());
            assertTrue(strings != null && new HashSet<>(strings).contains("gzip"));
        } finally {
            Utils.closeStream(inputStream);
        }
    }


}
