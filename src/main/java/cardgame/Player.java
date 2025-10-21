package cardgame;

import cardgame.exceptions.PlayerHasNoCardsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Player extends Thread {
    private final int number;
    private final List<Card> cards;
    private final List<String> actions;
    private final CardDeck pickupDeck;
    private final CardDeck discardDeck;
    private final int pickupIndex;
    private final int discardIndex;
    private final CountDownLatch countDownLatch;



    public Player(int number, CardDeck pickupDeck, CardDeck discardDeck, int discardIndex, CountDownLatch countDownLatch) {
        this.number = number;
        this.cards = Collections.synchronizedList(new ArrayList<>());
        this.actions = Collections.synchronizedList(new ArrayList<>());
        this.pickupDeck = pickupDeck;
        this.discardDeck = discardDeck;
        this.pickupIndex = number;
        this.discardIndex = discardIndex;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {

        //Decrement the latch that holds all threads
        this.countDownLatch.countDown();

        //Wait for the latch to be unlocked
        try {
            this.countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Handle player won instantly
        if (this.hasWon()) {
            this.actions.add("player " + (this.number + 1) + " wins");
            CardGame.isRunning.set(false);
            return;
        }


        while(CardGame.isRunning.get()) {
            try {

                //Handle pickup
                Card newCard = this.pickupDeck.getCard();

                //If pickup deck is empty hint to jvm to continue another thread
                if(newCard == null) {
                    Thread.yield();
                    continue;
                }

                this.pickupCard(newCard);
                this.actions.add("player %d draws a %d from deck %d".formatted(this.number + 1, newCard.getNumber(), this.pickupIndex));

                //Handle Discard
                Card oldCard = this.discardCard();
                this.discardDeck.addCard(oldCard);
                this.actions.add("player %d discards a %d to deck %d".formatted(this.number + 1, oldCard.getNumber(), this.discardIndex));
                this.actions.add("player %d current hand is %s".formatted(this.number + 1, this.getHandFormatted()));

                //Handle Player Won
                if (this.hasWon()) {
                    //Change Game and Winning Variables for other threads
                    CardGame.winningLatch.countDown();
                    CardGame.isRunning.set(false);
                    CardGame.winningPlayer.compareAndSet(-1, this.number + 1);

                    //Save last actions to action list
                    this.actions.add("player " + (this.number + 1) + " wins");
                    this.actions.add("player " + (this.number + 1) + " exits");
                    this.actions.add("player %d hand: %s".formatted(this.number + 1, this.getHandFormatted()));
                    return;
                }



            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        //Hold the thread until a thread has won, so that the winning players number can update
        try {
            CardGame.winningLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Save the last actions to action list
        int winningPlayer = CardGame.winningPlayer.get();
        this.actions.add("player " + winningPlayer + " has informed player " + getOutputNumber() + " that player " + winningPlayer + " has won");
        this.actions.add("player " + (this.number + 1) + " exits");
        this.actions.add("player %d hand: %s".formatted(this.number + 1, this.getHandFormatted()));
    }


    public Card discardCard() {

        //Player can't discard if they've got no cards
        if (this.cards.isEmpty()) {
            throw new PlayerHasNoCardsException();
        }

        //Filter out non-preferred cards into a new list
        List<Card> nonPreferredList = new ArrayList<>(this.cards.stream()
                .filter((x) -> x.getNumber() != this.number + 1)
                .toList());


        //Choose a random card to remove from the undesired ones and remove from list
        Random rand = new Random();
        int indexToRemove = rand.nextInt(nonPreferredList.size());
        Card cardToRemove = nonPreferredList.get(indexToRemove);
        this.cards.remove(cardToRemove);
        return cardToRemove;
    }

    public void pickupCard(Card newCard) {
        this.cards.add(newCard);
    }


    public boolean hasWon() {
        //Player should never have no cards
        if (this.cards.isEmpty()) {
            throw new PlayerHasNoCardsException();
        }

        //Check if all cards player has are the same
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
        //Formats the cards for readable output
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