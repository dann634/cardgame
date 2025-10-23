package cardgame;

import cardgame.io.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardGameTest {

    private CardGame cardGame;

    @BeforeEach
    void setUp() {
        this.cardGame = new CardGame();
        int playerCount = 100;
        List<String> fileData = FileManager.readFile("src/main/resources/packs/pack" + playerCount + ".txt");
        this.cardGame.startGame(playerCount, fileData);
    }


    @Test
    public void testPlayersHaveFourCardsAtEnd() {
        List<Player> players = this.cardGame.getPlayers();
        for(Player player : players) {
            if(player.getNumberOfCards() != 4) {
                fail("Player has less than 4 cards");
            }
        }


    }


}