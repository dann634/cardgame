package cardgame;

import cardgame.exceptions.PlayerHasNoCardsException;
import cardgame.exceptions.PlayerHasTooManyCardsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {
    private static final int MAX_CARD_LIMIT = 4;
    private int number;
    private List<Card> cards;

    public Player(int number) {
        this.number = number;
        this.cards = new ArrayList();
    }

    public synchronized Card discardCard() {
        if (this.cards.isEmpty()) {
            throw new PlayerHasNoCardsException();
        } else {
            List<Card> nonPreferredList = new ArrayList(this.cards.stream()
                    .filter((x) -> x.getNumber() != this.number + 1)
                    .toList());
            Random rand = new Random();
            int indexToRemove = rand.nextInt(nonPreferredList.size());
            Card cardToRemove = nonPreferredList.get(indexToRemove);
            this.cards.remove(cardToRemove);
            return cardToRemove;
        }
    }

    public synchronized void pickupCard(Card newCard) {
        this.cards.add(newCard);
    }

    public boolean hasWon() {
        if (this.cards.isEmpty()) {
            throw new PlayerHasNoCardsException();
        }

        Integer firstCardNumber = this.cards.get(0).getNumber();
        for(Card card : this.cards) {
            if(!card.getNumber().equals(firstCardNumber)) {
                return false;
            }
        }

        return true;
    }

    public int getOutputNumber() {
        return this.number + 1;
    }

    public String getHandFormatted() {
        StringBuilder handStr = new StringBuilder();
        for(Card card : this.cards) {
            handStr.append(card.getNumber()).append(" ");
        }

        return handStr.toString();
    }

    public int getNumberOfCards() {
        return this.cards.size();
    }

}