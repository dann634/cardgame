package cardgame;

import cardgame.exceptions.InvalidCardException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    //Tests that the value of a card cannot be null
    @Test
    public void testCardNumberCannotBeNull() {
        assertThrows(NullPointerException.class, () -> {
            new Card(null);
        });
    }

    //Tests that the value of a card cannot be less than zero
    @Test
    public void testCardNumberCannotBeLessThanZero() {
        assertThrows(InvalidCardException.class, () -> {
            new Card(-1);
        });
    }

    //Tests that a valid number for the card is accepted
    @Test
    public void testValidCardNumber() {
        Card card = new Card(5);
        assertEquals(5, card.getNumber());
    }
}