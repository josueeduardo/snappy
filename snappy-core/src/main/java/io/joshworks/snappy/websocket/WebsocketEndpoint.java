package io.joshworks.snappy.websocket;

import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;

/**
 * Created by josh on 3/8/17.
 */
public abstract class WebsocketEndpoint extends AbstractReceiveListener implements WebSocketConnectionCallback {

}
