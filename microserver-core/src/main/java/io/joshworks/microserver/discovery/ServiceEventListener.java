package io.joshworks.microserver.discovery;


import io.joshworks.microserver.discovery.common.Instance;

/**
 * Created by Josue on 10/07/2016.
 */
public interface ServiceEventListener {

    void onConnect(Instance instance);

    void onDisconnect(Instance instance);

    void newSession();
}
