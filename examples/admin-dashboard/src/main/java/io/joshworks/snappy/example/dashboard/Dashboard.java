package io.joshworks.snappy.example.dashboard;

import io.joshworks.snappy.extensions.dashboard.DashboardExtension;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static io.joshworks.snappy.SnappyServer.*;

/**
 * Created by Josh Gontijo on 4/27/17.
 */
public class Dashboard {

    private static final String LOG_PATH = System.getProperty("user.home") + File.separator + "sample.log";

    public static void main(String[] args) throws Exception {
        copyDummyLog();
        onShutdown(Dashboard::deleteDummyLog);

        register(new DashboardExtension());
        cors();
        start(); //http://localhost:9100/

    }

    //Used to show the tailf functionality, edit me on ~/sample.log and see the live changes on admin console
    private static void copyDummyLog() throws Exception {
        //copies a dummy log file to home directory
        File dest = new File(LOG_PATH);
        dest.createNewFile();

        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sample.log");
             FileOutputStream output = new FileOutputStream(dest)) {
            IOUtils.copy(input, output);
        }
    }

    private static void deleteDummyLog() {
        new File(LOG_PATH).delete();
    }
}
