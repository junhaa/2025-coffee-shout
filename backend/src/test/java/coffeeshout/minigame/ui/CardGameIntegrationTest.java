package coffeeshout.minigame.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    @Test
    void 게임을_시작한다() throws InterruptedException, TimeoutException {
        // given
        String SUBSCRIBE_GAME_STATE_ENDPOINT_FORMAT = "/topic/room/%d/gameState";
        String START_GAME_ENDPOINT_FORMAT = "/app/room/%d/cardGame/start";

        Long roomId = 1L;

        MessageCollector<TestDot> gameStateMessageCollector = subscribe(
                String.format(SUBSCRIBE_GAME_STATE_ENDPOINT_FORMAT, roomId),
                TestDot.class
        );

        // when
        send(String.format(START_GAME_ENDPOINT_FORMAT, roomId), "");

        // then
        List<TestDot> messages = List.of(
                gameStateMessageCollector.get(),
                gameStateMessageCollector.get(),
                gameStateMessageCollector.get()
        );

        assertThat(messages).containsExactlyInAnyOrder(
                new TestDot("test1"),
                new TestDot("test2"),
                new TestDot("test3")
        );
    }
}
