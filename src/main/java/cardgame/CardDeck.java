package cardgame;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CardDeck {

    private final BlockingQueue<Card> safeQueue;

    public CardDeck() {
        this.safeQueue = new LinkedBlockingQueue<>();
    }

    public Card getCard(long time, TimeUnit timeUnit) throws InterruptedException {
        return this.safeQueue.poll(time, timeUnit);
    }

    public void addCard(Card card) {
        this.safeQueue.offer(card);
    }

    public boolean isEmpty() {
        return this.safeQueue.isEmpty();
    }
}
