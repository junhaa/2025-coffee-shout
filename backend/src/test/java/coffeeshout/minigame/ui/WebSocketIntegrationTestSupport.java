package coffeeshout.minigame.ui;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class WebSocketIntegrationTestSupport {

    static final int CONNECT_TIMEOUT_SECONDS = 1;
    static final int RESPONSE_TIMEOUT_SECONDS = 5;
    static final String WEBSOCKET_BASE_URL_FORMAT = "ws://localhost:%d/ws";

    @LocalServerPort
    int port;
    WebSocketStompClient stompClient;
    String URL;
    CompletableFuture<String> completableFuture;
    StompSession session;

    @BeforeEach
    void setUp() throws ExecutionException, InterruptedException, TimeoutException {
        SockJsClient sockJsClient = new SockJsClient(List.of(
                new WebSocketTransport(new StandardWebSocketClient())
        ));

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new StringMessageConverter());
        URL = String.format(WEBSOCKET_BASE_URL_FORMAT, port);
        completableFuture = new CompletableFuture<>();
        session = stompClient
                .connectAsync(URL, new StompSessionHandlerAdapter() {})
                .get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    protected void subscribe(String subscribeEndpoint) {
        session.subscribe(subscribeEndpoint, new GenericStompFrameHandler());
    }

    protected String send(String sendEndpoint, Object bodyMessage) throws ExecutionException, InterruptedException, TimeoutException {
        session.send(String.format(sendEndpoint), bodyMessage);
        return completableFuture.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }


    class GenericStompFrameHandler implements StompFrameHandler {

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            completableFuture.complete((String) payload);
        }
    }
}
