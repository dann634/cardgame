package cardgame;

import cardgame.exceptions.PlayerHasNoCardsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Player extends Thread {
    private final int number;
    private final List<Card> cards;
    private final List<String> actions;
    private final CardDeck pickupDeck;
    private final CardDeck discardDeck;
    private int pickupIndex;
    private int discardIndex;



    public Player(int number, CardDeck pickupDeck, CardDeck discardDeck, int discardIndex) {
        this.number = number;
        this.cards = Collections.synchronizedList(new ArrayList<>());
        this.actions = Collections.synchronizedList(new ArrayList<>());
        this.pickupDeck = pickupDeck;
        this.discardDeck = discardDeck;
        this.pickupIndex = number;
        this.discardIndex = discardIndex;
    }

    @Override
    public void run() {

        while(!this.isInterrupted() && CardGame.isRunning.get()) {
            try {

                //Handle Player Won
                if (this.hasWon()) {
                    this.actions.add("player " + (this.number + 1) + " wins");
                    CardGame.isRunning.set(false);
                    break;
                }


                //Handle pickup
                Card newCard;
                synchronized (this.pickupDeck) {
                    newCard = this.pickupDeck.getCard();
                }

                if (newCard != null) {
                    this.pickupCard(newCard);
                    this.actions.add("player %d draws a %d from deck %d".formatted(this.number + 1, newCard.getNumber(), this.number + 1));

                    //Handle Discard
                    Card oldCard = this.discardCard();
                    synchronized (this.discardDeck) {
                        this.discardDeck.addCard(oldCard);
                    }
                    this.actions.add("player %d discards a %d to deck %d".formatted(this.number + 1, oldCard.getNumber(), this.discardIndex));
                    this.actions.add("player %d current hand is %s".formatted(this.number + 1, this.getHandFormatted()));

                }



//                Thread.sleep(5);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        this.actions.add("player " + (this.number + 1) + " exits");
        this.actions.add("player %d hand: %s".formatted(this.number + 1, this.getHandFormatted()));



    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    public void interruptGame(int playerNumberFinished) {
        this.actions.add("player " + playerNumberFinished + " has informed player " + getOutputNumber() + " that player " + playerNumberFinished + " has won");
        this.interrupt();
    }

    public Card discardCard() {

        if (this.cards.isEmpty()) {
            throw new PlayerHasNoCardsException();
        } else {
            List<Card> nonPreferredList = new ArrayList<>(this.cards.stream()
                    .filter((x) -> x.getNumber() != this.number + 1)
                    .toList());
            Random rand = new Random();
            int indexToRemove = rand.nextInt(nonPreferredList.size());
            Card cardToRemove = nonPreferredList.get(indexToRemove);
            this.cards.remove(cardToRemove);
            return cardToRemove;
        }
    }

    public void pickupCard(Card newCard) {
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

    public int getNumber() {
        return number;
    }

    public List<String> getActions() {
        return actions;
    }
}