package coffeeshout.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class StompMetricsInterceptor implements ChannelInterceptor {

    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeSessions = new AtomicInteger(0);

    public StompMetricsInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        Gauge.builder("websocket.stomp.active.sessions", activeSessions::get)
                .description("현재 연결된 사용자 수")
                .register(meterRegistry);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        switch (command) {
            case CONNECT:
                activeSessions.incrementAndGet();
                break;
            case DISCONNECT:
                activeSessions.decrementAndGet();
                break;
            case SUBSCRIBE:
                Counter.builder("websocket.stomp.subscriptions")
                        .tag("destination", accessor.getDestination())
                        .register(meterRegistry)
                        .increment();
                break;
            default:
                break;
        }
        return message;
    }
}
