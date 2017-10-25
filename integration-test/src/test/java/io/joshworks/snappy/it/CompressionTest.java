package io.joshworks.snappy.it;

import io.undertow.util.Headers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.joshworks.snappy.SnappyServer.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 7/6/17.
 */
public class CompressionTest {

    private static final String SERVER_URL = "http://localhost:9000";
    private static final String TEST_RESOURCE = "/gzip";

    private static final String dummyData = IntStream.range(0, 100000).boxed().map(i -> "A").collect(Collectors.joining("-"));

    @BeforeClass
    public static void setup() throws IOException {

        enableTracer();
        get(TEST_RESOURCE, exchange -> exchange.send(dummyData, "txt"));

        start();
    }

    @AfterClass
    public static void shutdown() {
        stop();
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
            closeQuietly(inputStream);
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
            closeQuietly(inputStream);
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
            closeQuietly(inputStream);
        }
    }

    private void closeQuietly(InputStream inputStream) {
        try {
            if (inputStream != null) {
                char[] buffer = new char[1024];
                try (Reader in = new InputStreamReader(inputStream, "UTF-8")) {
                    int read = 0;
                    while ((read = in.read(buffer)) > 0) {
                        //do nothing
                    }
                }
                inputStream.close();
            }

        } catch (Exception ex) {

        }
    }

}
