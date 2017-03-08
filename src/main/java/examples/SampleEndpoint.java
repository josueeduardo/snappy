package examples;

import com.josue.simpletow.websocket.WebsocketEndpoint;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.StreamSourceFrameChannel;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.IOException;

/**
 * Created by josh on 3/8/17.
 */
public class SampleEndpoint extends WebsocketEndpoint {

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        System.out.println("---------> CONNECTED");
    }

    @Override
    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
        super.onClose(webSocketChannel, channel);
        System.out.println("---------> onClose");
    }

    @Override
    protected void onError(WebSocketChannel channel, Throwable error) {
        super.onError(channel, error);
    }

    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
        String data = message.getData();
        System.out.println("---------> onFullTextMessage " + data);
        //        channel.getPeerConnections().forEach(pc ->  WebSockets.sendText("", pc, null));
        WebSockets.sendText("----> ECHO: " + data, channel, null);
        super.onFullTextMessage(channel, message);
    }


}
