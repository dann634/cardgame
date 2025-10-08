package cardgame;

import cardgame.exceptions.PlayerHasNoCardsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    @Test
    public void testPlayerCreatedWithEmptyHand() {
        Player player = new Player(0);
        assertEquals(0, player.getNumberOfCards());
    }

    @Test
    public void testPlayerPickUpCardAddsCardToHand() {
        Player player = new Player(0);
        player.pickupCard(new Card(5));
        assertEquals(1, player.getNumberOfCards());
    }

    @Test
    public void testPlayerGetHandFormattedPreservesOrder() {
        Player player = new Player(0);
        player.pickupCard(new Card(2));
        player.pickupCard(new Card(5));
        player.pickupCard(new Card(9));
        player.pickupCard(new Card(12));
        assertEquals("2 5 9 12 ", player.getHandFormatted());
    }

    @Test
    public void testPlayerDiscardCardRemovesNonPreferredCard() {
        Player player = new Player(0);
        player.pickupCard(new Card(1));
        player.pickupCard(new Card(2));

        Card discardedCard = player.discardCard();
        assertAll(
                () -> assertEquals(2, discardedCard.getNumber()),
                () -> assertEquals(1, player.getNumberOfCards())
        );
    }

    @Test
    public void testPlayerDiscardCardRemovesDiscardedCard() {
        Player player = new Player(0);
        player.pickupCard(new Card(1));
        player.pickupCard(new Card(2));
        player.discardCard();

        assertFalse(player.getHandFormatted().contains("2"));
    }

    @Test
    public void testPlayerDiscardCardRemovesRandomNonPreferredCard() {
        Player player = new Player(0);
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

    @Test
    public void testPlayerHasNoCardsDiscardCard() {
        assertThrows(PlayerHasNoCardsException.class, () -> new Player(0).discardCard());
    }

    @Test
    public void testPlayerHasWonReturnsTrue() {
        Player player = new Player(0);
        for (int i = 0; i < 4; i++) {
            player.pickupCard(new Card(1));
        }
        assertTrue(player.hasWon());
    }

}
