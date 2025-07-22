package coffeeshout.minigame.ui;

import coffeeshout.minigame.application.CardGameService;
import coffeeshout.minigame.domain.MiniGameResult;
import coffeeshout.minigame.domain.cardgame.CardGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CardGameController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CardGameService cardGameService;

    @MessageMapping("/room/{roomId}/cardGame/start")
    public void startGame(@DestinationVariable Long roomId) {
        cardGameService.start(roomId);

        final CardGame cardGame = cardGameService.getCardGame(roomId);

        CardGameStateMessage response = CardGameStateMessage.of(cardGame, roomId);

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/gameState",
                new TestDot("test1"));

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/gameState",
                new TestDot("test2"));

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/gameState",
                new TestDot("test3"));
    }

    @MessageMapping("/room/{roomId}/cardGame/select")
    public void selectCard(@DestinationVariable Long roomId, @Payload CardGameSelectMessage message) {
        cardGameService.selectCard(roomId, message.playerName(), message.cardIndex());

        final CardGame cardGame = cardGameService.getCardGame(roomId);

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/gameState",
                CardGameStateMessage.of(cardGame, roomId));

        cardGameService.checkAndMoveRound(roomId);
    }

    @MessageMapping("/cardGame/rank")
    public void getRank(CardGameRankMessage message) {
        final MiniGameResult miniGameResult = cardGameService.getMiniGameResult(message.roomId());

        messagingTemplate.convertAndSend("/topic/room/" + message.roomId() + "/  ",
                coffeeshout.minigame.ui.MiniGameRanksMessage.from(miniGameResult));
    }
}
