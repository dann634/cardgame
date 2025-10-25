package cardgame;

import cardgame.exceptions.PlayerHasNoCardsException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    private final Logger logger;



    public Player(int number, CardDeck pickupDeck, CardDeck discardDeck, int discardIndex, CountDownLatch countDownLatch) {
        this.number = number;
        this.cards = new ArrayList<>();
        this.actions = new ArrayList<>();//Collections.synchronizedList(new ArrayList<>());
        this.pickupDeck = pickupDeck;
        this.discardDeck = discardDeck;
        this.pickupIndex = number;
        this.discardIndex = discardIndex;
        this.countDownLatch = countDownLatch;
        try {
            this.logger = new Logger(number + 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        try(logger) {
            //Handle player won instantly
            if (this.hasWon()) {
                this.actions.add("player " + (this.number + 1) + " wins");
                logger.log("player " + (this.number + 1) + " wins");
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
                    String pickupMessage = "player %d draws a %d from deck %d".formatted(this.number + 1, newCard.getNumber(), this.pickupIndex);
                    this.actions.add(pickupMessage);
                    logger.log(pickupMessage);


                    //Handle Discard
                    Card oldCard = this.discardCard();
                    this.discardDeck.addCard(oldCard);

                    String discardMessage = "player %d discards a %d to deck %d".formatted(this.number + 1, oldCard.getNumber(), this.discardIndex);
                    this.actions.add(discardMessage);
                    logger.log(discardMessage);

                    String currentHandMessage = "player %d current hand is %s".formatted(this.number + 1, this.getHandFormatted());
                    this.actions.add(currentHandMessage);
                    logger.log(currentHandMessage);

                    //Handle Player Won
                    if (this.hasWon()) {
                        //Change Game and Winning Variables for other threads
                        CardGame.isRunning.set(false);
                        CardGame.winningPlayer.compareAndSet(-1, this.number + 1);
                        CardGame.winningLatch.countDown();

                        //Save last actions to action list
                        this.actions.add("player " + (this.number + 1) + " wins");
                        this.actions.add("player " + (this.number + 1) + " exits");
                        this.actions.add("player %d hand: %s".formatted(this.number + 1, this.getHandFormatted()));

                        logger.log("player " + (this.number + 1) + " wins");
                        logger.log("player " + (this.number + 1) + " exits");
                        logger.log("player %d hand: %s".formatted(this.number + 1, this.getHandFormatted()));
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

//            while(winningPlayer == -1) {
//                winningPlayer = CardGame.winningPlayer.get();
//            }

            this.actions.add("player " + winningPlayer + " has informed player " + getOutputNumber() + " that player " + winningPlayer + " has won");
            this.actions.add("player " + (this.number + 1) + " exits");
            this.actions.add("player %d hand: %s".formatted(this.number + 1, this.getHandFormatted()));

            logger.log("player " + winningPlayer + " has informed player " + getOutputNumber() + " that player " + winningPlayer + " has won");
            logger.log("player " + (this.number + 1) + " exits");
            logger.log("player %d hand: %s".formatted(this.number + 1, this.getHandFormatted()));
        }

        catch (Exception e) {
            e.printStackTrace();
        }
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

    private static class Logger implements AutoCloseable {

        private final FileChannel channel;

        public Logger(int playerNumber) throws IOException {
            Path path = Paths.get("src/main/resources/output/player" + playerNumber + ".txt");
            this.channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        public void log(String message) throws IOException {
            message += "\n";
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            while(buffer.hasRemaining()) {
                channel.write(buffer);
            }
        }

        @Override
        public void close() throws Exception {
            channel.close();
        }
    }

}