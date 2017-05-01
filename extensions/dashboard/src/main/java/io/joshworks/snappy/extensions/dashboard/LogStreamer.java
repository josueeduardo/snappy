package io.joshworks.snappy.extensions.dashboard;

import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Created by Josh Gontijo on 5/1/17.
 */
public class LogStreamer implements ServerSentEventConnectionCallback {

    private static final Logger logger = LoggerFactory.getLogger(LogStreamer.class);

    private final Set<Tailer> tailers = new HashSet<>();

    private final ExecutorService executor;
    private final String logLocation;

    public LogStreamer(ExecutorService executor, String logLocation) {
        this.executor = executor;
        this.logLocation = logLocation;
    }

    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        Deque<String> params = connection.getQueryParameters().get("tailf");
        String tailfParam = params == null || params.isEmpty() ? Boolean.FALSE.toString() : params.getFirst();

        try {
            boolean tailf = Boolean.parseBoolean(tailfParam);
            LogTailer listener = new LogTailer(logLocation, tailf);
            Tailer tailer = Tailer.create(listener.file, listener, 1000, tailf);
            connection.addCloseTask(channel -> {
                tailer.stop();
                tailers.remove(tailer);
            });
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

    public void stopStreaming() {
        tailers.forEach(Tailer::stop);
    }
}
