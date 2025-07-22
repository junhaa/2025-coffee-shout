package coffeeshout.minigame.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class CardGameIntegrationTest extends WebSocketIntegrationTestSupport {

    @Test
    void 게임을_시작한다() throws ExecutionException, InterruptedException, TimeoutException {

        String SUBSCRIBE_GAME_STATE_ENDPOINT_FORMAT = "/topic/room/%d/gameState";
        String START_GAME_ENDPOINT_FORMAT = "/app/room/%d/cardGame/start";

        Long roomId = 1L;

        subscribe(SUBSCRIBE_GAME_STATE_ENDPOINT_FORMAT);
        String result = send(String.format(START_GAME_ENDPOINT_FORMAT, roomId), "");

        assertThat(result).isEqualTo("성공");
    }
}
