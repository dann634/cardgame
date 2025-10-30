package cardgame;

import cardgame.io.FileManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardGameTest {

    private CardGame cardGame;
    private static int playerCount = 20;

    @BeforeAll
    public static void firstSetUp() {
        new CardGame().createPack(playerCount);
    }

    @BeforeEach
    void setUp() {
        this.cardGame = new CardGame();
        List<String> fileData = FileManager.readFile("src/main/resources/packs/pack" + playerCount + ".txt");
        this.cardGame.startGame(playerCount, fileData);
    }


    //Tests that all the players finish the game holding 4 cards (tests atomicity)
    @Test
    public void testPlayersHaveFourCardsAtEnd() {
        List<Player> players = this.cardGame.getPlayers();
        for(Player player : players) {
            if(player.getNumberOfCards() != 4) {
                fail("Player has less than 4 cards");
            }
        }
    }

    //Tests that all players receive a message from the other players when someone wins
    @Test
    public void testAllPlayersReceiveWinMessage() {
        List<Player> players = this.cardGame.getPlayers();
        int winningNumber = CardGame.winningPlayer.get();
        for(Player player : players) {
            List<String> playerActions = player.getActions();
            String winningMessage = String.format("player %d has informed player %d that player %d has won", winningNumber, player.getOutputNumber(), winningNumber);
            String playerMessage = playerActions.get(playerActions.size() - 3);
            if(player.getOutputNumber() != winningNumber && !playerMessage.equals(winningMessage)) {
                fail("Player didn't receive winning message");
            }
        }
    }

    //Tests that the player who wins has the 'Player x wins!' in their action log
    @Test
    public void testWinnerPrintsWinningMessage() {
        List<Player> players = this.cardGame.getPlayers();
        int winningNumber = CardGame.winningPlayer.get();
        Player player = players.get(winningNumber - 1);
        List<String> playerActions = player.getActions();
        assertEquals("player " + winningNumber + " wins", playerActions.get(playerActions.size() - 3));
    }

    //Tests that all the output files for the decks are generated
    @Test
    public void testDeckOutputFilesAreGenerated() {
        int deckCount = this.cardGame.getCardDecks().length;
        int deckCounter = 1;
        for(int i = 0; i <= deckCount; i++) {
            Path path = Paths.get("src/main/resources/output/deck" + deckCounter + "_output.txt");
            if(!Files.exists(path)) {
                fail("Deck File " + deckCounter + " not found");
            }
        }
    }

    //Tests that all the output files for the players actions are generated properly
    @Test
    public void testPlayerOutputFilesAreGenerated() {
        int playerCount = this.cardGame.getPlayers().size();
        int playerCounter = 1;
        for(int i = 0; i <= playerCount; i++) {
            Path path = Paths.get("src/main/resources/output/player" + playerCounter + "_output.txt");
            if(!Files.exists(path)) {
                fail("Player File " + playerCounter + " not found");
            }
        }
    }

    //Tests that the pack file loads a valid test Pack
    @Test
    public void testPackFileLoadsCorrectly() {
        List<String> testFileData = new ArrayList<>(List.of("1", "1", "1", "1", "2", "2", "2", "2"
        ,"3","3","3","3","4","4","4","4"));
        this.cardGame.isPackValid(testFileData);
    }

    //Tests that an invalid pack file is rejected
    @Test
    public void testInvalidPackFile() {
        List<String> testFileData = new ArrayList<>(List.of("1", "1", "1", "2", "2", "2"));
        this.cardGame.isPackValid(testFileData);
    }


}