package cardgame;

import cardgame.exceptions.InvalidCardException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
    @Test
    public void testCardNumberCannotBeNull() {
        assertThrows(NullPointerException.class, () -> {
            new Card(null);
        });
    }

    @Test
    public void testCardNumberCannotBeLessThanZero() {
        assertThrows(InvalidCardException.class, () -> {
            new Card(-1);
        });
    }

    @Test
    public void testValidCardNumber() {
        Card card = new Card(5);
        assertEquals(5, card.getNumber());
    }
}