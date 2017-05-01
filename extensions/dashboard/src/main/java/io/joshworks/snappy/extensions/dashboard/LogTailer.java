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
    private static final long MAX_FILE_SIZE = 10; //mb

    final File file;

    public LogTailer(String filePath, boolean tailf) {
        this.file = getFile(filePath, tailf);
    }

    private File getFile(String fileName, boolean tailf) {
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
        if (!tailf && file.length() > (MAX_FILE_SIZE * 1048576)) {
            throw new RuntimeException("File is too big, max allowed is " + MAX_FILE_SIZE + "mb");
        }
        return file;
    }

    @Override
    public void handle(String line) {
        SseBroadcaster.broadcast(line);
    }

}
