package io.joshworks.snappy.extensions.ssr.client;

import io.joshworks.snappy.extensions.ssr.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.joshworks.snappy.extensions.ssr.SSRKeys.SSR_LOGGER;

/**
 * Created by Josh Gontijo on 6/5/17.
 */
public final class Services {

    private static final Logger logger = LoggerFactory.getLogger(SSR_LOGGER);

    private static Services INSTANCE;
    private ServiceStore store;

    private Services(ServiceStore store) {

        this.store = store;
    }

    synchronized static void init(ServiceStore store) {
        if (INSTANCE == null) {
            INSTANCE = new Services(store);
        }
    }

    public String getUrl(String serviceName) {
        if (INSTANCE == null) {
            throw new IllegalStateException("Service store not initialized yet");
        }
        Instance instance = Services.INSTANCE.store.get(serviceName);
        if (instance == null) {
            logger.warn("Service not found for name '{}'", serviceName);
            return null;
        }
        return instance.getAddress();
    }

}
