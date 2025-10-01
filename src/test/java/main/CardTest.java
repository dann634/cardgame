package java.main;

import main.java.cardgame.Card;
import org.junit.Test;
import static org.junit.Assert.*;


public class CardTest {
    @Test
    public void constructorTest_validNumber() {
        Card card = new Card(5);
        assertEquals(Integer.valueOf(5), card.getNumber());
    }

    @Test
    public void constructorTest_nullNumber() {
        new Card(null);
    }

    @Test
    public void constructorTest_invalidNumber() {
        new Card(-1);
    }
}