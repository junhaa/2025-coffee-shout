package coffeeshout.minigame.ui;

import coffeeshout.minigame.domain.cardgame.CardGame;
import coffeeshout.minigame.domain.cardgame.CardHand;
import coffeeshout.minigame.domain.cardgame.card.Card;
import coffeeshout.player.domain.Player;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public record MiniGameStateMessage(
        Long roomId,
        int currentRound,
        Map<Card, String> playerSelections,
        Boolean allSelected
) {

    public static MiniGameStateMessage of(final CardGame cardGame, final Long roomId) {

        final Map<Card, String> playerSelections = generatePlayerSelections(cardGame);
        return new MiniGameStateMessage(
                roomId,
                cardGame.getRound().ordinal(),
                playerSelections,
                cardGame.isFinished(cardGame.getRound())
        );
    }

    private static Map<Card, String> generatePlayerSelections(CardGame cardGame) {
        Map<Card, String> result = new HashMap<>();
        System.out.println(result);
        return result;
    }

    private static String findCardHolderName(CardGame cardGame, Card card) {
//        for (Entry<Player, CardHand> playerCardsEntry : cardGame.getPlayerHands().entrySet()) {
//            if (hasSameCardInCurrentRound(cardGame, card, playerCardsEntry)) {
//                return playerCardsEntry.getKey().getName();
//            }
//        }
        return null;
    }

    private static boolean hasSameCardInCurrentRound(CardGame cardGame, Card card,
                                                     Entry<Player, List<Card>> playerCardsEntry) {
        return playerCardsEntry.getValue().get(cardGame.getRound().ordinal()).equals(card);
    }

    private static Map<String, Integer> generatePlayerScores(CardGame cardGame) {
        final Map<String, Integer> scores = new HashMap<>();

        cardGame.calculateScores()
                .forEach((player, score) -> scores.put(player.getName(), score.getResult()));

        return scores;
    }
}
