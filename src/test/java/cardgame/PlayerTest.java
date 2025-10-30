package cardgame;

import cardgame.exceptions.PlayerHasNoCardsException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private CountDownLatch latch;
    private CardDeck[] decks;


    //Tests that when a player object is initialised they have an empty hand
    @Test
    public void testPlayerCreatedWithEmptyHand() {
        this.setupTest(1);
        Player player = new Player(0, null, null, 0, latch);
        assertEquals(0, player.getNumberOfCards());
    }

    //Tests that when a player picks up a card it's added to their internal list
    @Test
    public void testPlayerPickUpCardAddsCardToHand() {
        setupTest(1);
        Player player = new Player(0, new CardDeck(), null, 1, latch);
        player.pickupCard(new Card(5));
        assertEquals(1, player.getNumberOfCards());
    }

    //Tests that the String output of the cards keeps the order of the list
    @Test
    public void testPlayerGetHandFormattedPreservesOrder() {
        setupTest(1);
        Player player = new Player(0, new CardDeck(), null, 1, latch);
        player.pickupCard(new Card(2));
        player.pickupCard(new Card(5));
        player.pickupCard(new Card(9));
        player.pickupCard(new Card(12));
        assertEquals("2 5 9 12 ", player.getHandFormatted());
    }

    //Tests that when the player needs to discard a card, they discard one that isn't their player number
    @Test
    public void testPlayerDiscardCardRemovesNonPreferredCard() {
        Player player = new Player(0, new CardDeck(), new CardDeck(), 1, latch);
        player.pickupCard(new Card(1));
        player.pickupCard(new Card(2));

        Card discardedCard = player.discardCard();
        assertAll(
                () -> assertEquals(2, discardedCard.getNumber()),
                () -> assertEquals(1, player.getNumberOfCards())
        );
    }

    //Tests that the player actually removes the discarded card from their internal list
    @Test
    public void testPlayerDiscardCardRemovesDiscardedCard() {
        setupTest(1);
        Player player = new Player(0, new CardDeck(), new CardDeck(), 1, latch);
        player.pickupCard(new Card(1));
        player.pickupCard(new Card(2));
        player.discardCard();

        assertFalse(player.getHandFormatted().contains("2"));
    }

    //Tests that the player discards a non-preferred card
    @Test
    public void testPlayerDiscardCardRemovesRandomNonPreferredCard() {
        setupTest(1);
        Player player = new Player(0, new CardDeck(), new CardDeck(), 1, latch);
        player.pickupCard(new Card(1));
        player.pickupCard(new Card(6));
        player.pickupCard(new Card(8));
        player.pickupCard(new Card(5));

        Card discardedCard = player.discardCard();
        assertAll(
                () -> assertNotEquals(1, discardedCard.getNumber()),
                () -> assertEquals(3, player.getNumberOfCards())
        );
    }

    //Tests that a player cannot discard a card when they have none
    @Test
    public void testPlayerHasNoCardsDiscardCard() {
        setupTest(1);
        assertThrows(PlayerHasNoCardsException.class, () -> new Player(0, null, null, 1, latch).discardCard());
    }

    //Tests the initial win condition for a player
    @Test
    public void testPlayerHasWonReturnsTrue() {
        setupTest(1);
        Player player = new Player(0, new CardDeck(), new CardDeck(), 1, latch);
        for (int i = 0; i < 4; i++) {
            player.pickupCard(new Card(1));
        }
        assertTrue(player.hasWon());
    }

    public void setupTest(int playerCount) {
        this.latch = new CountDownLatch(playerCount);
        this.decks = new CardDeck[playerCount];
        for (int i = 0; i < playerCount; i++) {
            this.decks[i] = new CardDeck();
        }
    }


}
