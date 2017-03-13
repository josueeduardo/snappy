package io.joshworks.snappy.discovery.locator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Josue on 26/08/2016.
 */
public class EC2Discovery implements Discovery {

    private static final String AWS_META_URL = "http://169.254.169.254/latest/meta-data/";
    private static final String PUBLIC_HOSTNAME = "public-hostname";
    private static final String INSTANCE_ID = "instance-id";

    private String address;
    private String hostName;

    public EC2Discovery() {
        //TODO check instance-id or public-ipv4
        //TODO check public or local IP
        address = getMetadata(AWS_META_URL + INSTANCE_ID);
        hostName = getMetadata(AWS_META_URL + PUBLIC_HOSTNAME);
    }

    @Override
    public String resolveHost(boolean useHostname) {
        return useHostname ? hostName : address;
    }

    private String getMetadata(String awsUrl) {
        try {
            URL url = new URL(awsUrl);
            URLConnection conn = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return reader.readLine();
        } catch (Exception e) {
            throw new RuntimeException("Could not resolve EC2 host address", e);
        }
    }
}
