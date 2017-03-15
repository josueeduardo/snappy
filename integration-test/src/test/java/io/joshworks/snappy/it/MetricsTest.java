package io.joshworks.snappy.it;

import com.mashape.unirest.http.HttpResponse;
import io.joshworks.snappy.Config;
import io.joshworks.snappy.SnappyServer;
import io.joshworks.snappy.client.RestClient;
import io.joshworks.snappy.metric.MetricData;
import io.joshworks.snappy.metric.RestMetricHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class MetricsTest {

    private static SnappyServer server = new SnappyServer(new Config().enableHttpMetrics());

    @BeforeClass
    public static void start() {


        server.get("/test", (exchange) -> {
        });


        server.start();
    }

    @AfterClass
    public static void shutdown() {
        server.stop();
    }

    @Test
    public void okResults() throws Exception {
        int status = RestClient.get("http://localhost:8080/test").asString().getStatus();
        assertEquals(200, status);

        HttpResponse<MetricData> response = RestClient.get("http://localhost:8080/metrics").asObject(MetricData.class);
        assertEquals(200, response.getStatus());

        MetricData metrics = response.getBody();
        assertNotNull(metrics);
        assertEquals(1, metrics.getResources().size());

        Optional<RestMetricHandler.RestMetrics> foundMetrics = metrics.getResources().stream()
                .filter(m -> m.getUrl().equals("/test"))
                .findFirst();

        assertTrue(foundMetrics.isPresent());
        RestMetricHandler.RestMetrics metric = foundMetrics.get();
        assertEquals(1L, metric.getMetrics().getTotalRequests());
        assertEquals(1, metric.getMetrics().getResponseCodes().size());
        assertEquals(1, metric.getMetrics().getResponseCodes().get("200").get()); //200 OK

    }

}
