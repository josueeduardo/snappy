package io.joshworks.snappy.extensions.dashboard.log;

import io.joshworks.snappy.sse.SseBroadcaster;
import io.joshworks.snappy.sse.SseContext;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by Josh Gontijo on 5/1/17.
 */
public class LogStreamer {

    private static final Logger logger = LoggerFactory.getLogger(LogStreamer.class);

    private static final String TAILF_PARAM = "tailf";
    private final Map<SseContext, Tailer> tailers = new HashMap<>();

    private final ExecutorService executor;
    private final String logLocation;
    private final SseBroadcaster broadcaster;

    public LogStreamer(ExecutorService executor, String logLocation, SseBroadcaster broadcaster) {
        this.executor = executor;
        this.logLocation = logLocation;
        this.broadcaster = broadcaster;
    }

    public void handle(SseContext sse) {
        sse.onClose(this::stopStreaming);
        try {
            boolean tailf = sse.queryParameterVal(TAILF_PARAM).asBoolean().orElse(false);
            LogTailer listener = new LogTailer(logLocation, tailf, broadcaster);
            Tailer tailer = new Tailer(listener.file, listener, 500, tailf);
            tailers.put(sse, tailer);
            executor.execute(tailer);
        } catch (Exception e) {
            logger.error("Error opening log file", e);
            sse.send(e.getMessage(), new ServerSentEventConnection.EventCallback() {
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

    private void stopTailer(SseContext ctx) {
        Tailer removed = tailers.remove(ctx);
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
            key.close();
        });
    }
}
