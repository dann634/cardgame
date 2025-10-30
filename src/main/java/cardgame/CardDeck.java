package cardgame;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CardDeck {

    private final ConcurrentLinkedQueue<Card> safeQueue;

    public CardDeck() {
        this.safeQueue = new ConcurrentLinkedQueue<>();
    }

    public Card getCard() throws InterruptedException {
        return this.safeQueue.poll();
    }

    public void addCard(Card card) {
        this.safeQueue.offer(card);
    }

    public int size() {return this.safeQueue.size();}

    public String getOutputString() {
        StringBuilder output = new StringBuilder();
        for (Card card : this.safeQueue) {
            output.append(card.toString()).append(" ");
        }
        return output.toString();
    }
}
