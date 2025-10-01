package main.java.cardgame;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CardDeck {

    private final ConcurrentLinkedQueue<Card> safeQueue;

    public CardDeck() {
        this.safeQueue = new ConcurrentLinkedQueue<>();
    }

    public Card getCard() throws NullPointerException {
        return this.safeQueue.poll();
    }

    public void addCard(Card card) {
        this.safeQueue.add(card);
    }

    public boolean isEmpty() {
        return this.safeQueue.isEmpty();
    }
}
