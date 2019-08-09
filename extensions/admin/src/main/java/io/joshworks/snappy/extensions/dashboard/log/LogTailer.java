package io.joshworks.snappy.extensions.dashboard.log;

import io.joshworks.snappy.sse.SseBroadcaster;
import org.apache.commons.io.input.TailerListenerAdapter;

import java.io.File;

/**
 * Created by Josh Gontijo on 5/1/17.
 */
public class LogTailer extends TailerListenerAdapter {

    private static final String LOG_EXTENSION = ".log";
    private static final long MAX_FILE_SIZE = 10; //mb

    final File file;
    private SseBroadcaster broadcaster;

    public LogTailer(String filePath, boolean tailf, SseBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
        this.file = getFile(filePath, tailf);
    }

    private File getFile(String fileName, boolean tailf) {
        if (fileName == null || fileName.isEmpty()) {
            throw new RuntimeException("Log location not configured, use '" + file.getPath() + "'");
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
        broadcaster.broadcast(line);
    }

}
