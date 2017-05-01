package io.joshworks.snappy.extensions.dashboard;

import io.joshworks.snappy.sse.SseBroadcaster;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;

import static io.joshworks.snappy.extensions.dashboard.DashboardExtension.LOG_LOCATION;

/**
 * Created by Josh Gontijo on 5/1/17.
 */
public class LogTailer extends TailerListenerAdapter {

    private static final String LOG_EXTENSION = ".log";

    private final boolean end;
    final File file;

    public LogTailer(boolean end, String filePath) {
        this.end = end;
        this.file = getFile(filePath);
    }

    private File getFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new RuntimeException("Log location not configured, use '" + LOG_LOCATION + "'");
        }
        File file = new File(fileName);
        if (!file.exists()) {
            throw new RuntimeException("File '" + fileName + "' doesn't exist");
        }
        if (file.isDirectory() || !file.getName().endsWith(LOG_EXTENSION)) {
            throw new RuntimeException("Not a valid '" + LOG_EXTENSION + "' file, name: '" + fileName + "'");
        }
        return file;
    }

    @Override
    public void handle(String line) {
        SseBroadcaster.broadcast(line);
    }

}
