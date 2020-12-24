package academy.kafka;

import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import static io.undertow.Handlers.path;
import static io.undertow.Handlers.resource;
import static io.undertow.Handlers.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xnio.ChannelListener;

public class WebsocketService01 {

    private static final String WEBPAGE = "web/index.html";
    private static final String ApplicationMame = "myChatApp";

    private static final List<WebSocketChannel> channels = Collections
            .synchronizedList(new ArrayList<WebSocketChannel>());

    public static void main(final String[] args) {
        final String host = "localhost";
        final int port = 8091;
        URI uri = URI.create("http://" + host + ":" + port);
        System.out
                .println("To see chat in action is to open two different browsers and point them at " + uri.toString());

        Undertow server = Undertow.builder().addHttpListener(port, "localhost")
                .setHandler(path().addPrefixPath(ApplicationMame, websocket(new WebSocketConnectionCallback() {
                    @Override
                    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                        System.out.println("onConnect");
                        final String chatboxName = getChatboxFromUrl(channel, ApplicationMame);
                        setChatboxTag(channel, chatboxName);
                        synchronized (channels) {
                            channels.add(channel);
                            switch (chatboxName) {
                                case "myTopic": {
                                    channel.getReceiveSetter().set(new AbstractReceiveListener() {
                                        @Override
                                        protected void onFullTextMessage(WebSocketChannel channel,
                                                BufferedTextMessage message) {
                                            String msg = message.getData();
                                            channels.forEach((ch) -> {
                                                WebSockets.sendText(msg, ch, null);
                                            });
                                        }
                                    });
                                    break;
                                }
                                default: {
                                    System.err.println("chatbox:>>" + chatboxName + "<< not found");
                                }
                            }
                        }
                        channel.addCloseTask(new ChannelListener<WebSocketChannel>() {
                            @Override
                            public void handleEvent(WebSocketChannel chatbox) {
                                synchronized (channels) {
                                    System.out.println(String.format("chatbox %s disconnects", getChatboxTag(chatbox)));

                                    channels.remove(chatbox);
                                }
                            }
                        });
                        channel.resumeReceives();
                    }

                })).addPrefixPath("/", resource(new ClassPathResourceManager(WebsocketService01.class.getClassLoader()))
                        .addWelcomeFiles(WEBPAGE)))
                .build();

        server.start();

        try {
            java.awt.Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getChatboxFromUrl(WebSocketChannel chatbox, String prefix) {
        String url = chatbox.getUrl();
        System.out.println("url=" + url.toString() + "prefix=" + prefix);
        return chatbox.getUrl().substring(url.indexOf(prefix) + prefix.length() + 1);
    }

    private static void setChatboxTag(WebSocketChannel chatbox, String chatboxName) {
        chatbox.setAttribute("chat", chatboxName);
    }

    private static String getChatboxTag(WebSocketChannel chatbox) {
        return (String) chatbox.getAttribute("chat");
    }
}
