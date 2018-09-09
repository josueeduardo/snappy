package io.joshworks.snappy.extensions.dashboard.log;

import io.joshworks.snappy.sse.SseCallback;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by Josh Gontijo on 5/1/17.
 */
public class LogStreamer extends SseCallback {

    private static final Logger logger = LoggerFactory.getLogger(LogStreamer.class);

    private static final String TAILF_PARAM = "tailf";
    private final Map<ServerSentEventConnection, Tailer> tailers = new HashMap<>();

    private final ExecutorService executor;
    private final String logLocation;

    public LogStreamer(ExecutorService executor, String logLocation) {
        this.executor = executor;
        this.logLocation = logLocation;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        Deque<String> params = connection.getQueryParameters().get(TAILF_PARAM);
        String tailfParam = params == null || params.isEmpty() ? Boolean.FALSE.toString() : params.getFirst();

        try {
            boolean tailf = Boolean.parseBoolean(tailfParam);
            LogTailer listener = new LogTailer(logLocation, tailf);
            Tailer tailer = new Tailer(listener.file, listener, 500, tailf);
            tailers.put(connection, tailer);
            executor.execute(tailer);
        } catch (Exception e) {
            logger.error("Error opening log file", e);
            connection.send(e.getMessage(), new ServerSentEventConnection.EventCallback() {
                @Override
                public void done(ServerSentEventConnection connection, String data, String event, String id) {
                    try {
                        connection.close();
                    } catch (IOException e1) {
                        logger.error("Error opening log file", e);
                    }
                }

                @Override
                public void failed(ServerSentEventConnection connection, String data, String event, String id, IOException e) {

                }
            });
        }
    }

    @Override
    public void onClose(ServerSentEventConnection connection) {
        stopTailer(connection);
    }

    private void stopTailer(ServerSentEventConnection connection) {
        Tailer removed = tailers.remove(connection);
        if (removed != null) {
            removed.stop();
        }
    }

    public void stopStreaming() {
        tailers.forEach((key, value) -> {

            try {
                stopTailer(key);
            } catch (Exception e) {
                logger.warn("Failed to stop file tailer");
            }

            try {
                key.close();
            } catch (IOException e) {
                logger.warn("Failed to close connection");
            }
        });
    }
}
