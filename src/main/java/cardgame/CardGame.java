package main.java.cardgame;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CardGame {
    private final Scanner scan;
    private volatile CardDeck[] cardDecks;
    private volatile boolean isRunning = false;
    private volatile boolean isReady = false;
    private final List<Thread> userThreads;

    public static void main(String[] args) {
        CardGame cardGame = new CardGame();
        cardGame.startGame();
    }

    public CardGame() {
        this.scan = new Scanner(System.in);
        this.userThreads = new ArrayList();
    }

    public void startGame() {
        int playerCount = this.getPlayerCount();
        this.cardDecks = new CardDeck[playerCount];

        int i;
        for(i = 0; i < playerCount; ++i) {
            this.cardDecks[i] = new CardDeck();
        }

        for(i = 0; i < playerCount; i++) {
            try {
                this.addPlayer(i, playerCount);
            } catch (InterruptedException e) {
                System.out.println("Error: Couldn't start player thread");
            }
        }

        this.isReady = true;
        this.isRunning = true;
    }

    private int getPlayerCount() {
        int players = 0;

        while(players <= 0) {
            try {
                System.out.println("Please enter the number of players");
                players = this.scan.nextInt();
            } catch (RuntimeException e) {
                System.out.println("Error: Please enter a non-negative integer");
            }
        }

        return players;
    }

    private void addPlayer(int playerNumber, int totalPlayers) throws InterruptedException {
        Thread thread = new Thread(() -> {
            List<String> outputFileArray = new ArrayList();
            Player player = new Player(playerNumber);

            while(!this.isReady) {
            }

            outputFileArray.add("player %d initial hand %s".formatted(player.getOutputNumber(), player.getHandFormatted()));

            while(this.isRunning) {

                //Handle pickup
                System.out.printf("Player %d is running%n", player.getOutputNumber());
                Card newCard = this.cardDecks[playerNumber].getCard();
                if (newCard != null) {
                    player.pickupCard(newCard);
                    outputFileArray.add("player %d draws a %d from deck %d%n".formatted(player.getOutputNumber(), newCard.getNumber(), player.getOutputNumber()));
                }

                //Handle Discard
                int discardDeckIndex = playerNumber == totalPlayers ? 0 : playerNumber + 1;
                Card oldCard = player.discardCard();
                this.cardDecks[discardDeckIndex].addCard(oldCard);
                outputFileArray.add("player %d discards a %d to deck %d%n".formatted(player.getOutputNumber(), oldCard.getNumber(), discardDeckIndex + 1));
                outputFileArray.add("player %d current hand is %s".formatted(player.getOutputNumber(), player.getHandFormatted()));

                //Handle Player Won
                if (player.hasWon()) {
                    outputFileArray.add("player " + player.getOutputNumber() + " won");
                    this.isRunning = false;
                }
            }

            outputFileArray.add("player " + player.getOutputNumber() + " exits");
            outputFileArray.add("player %d hand: %s".formatted(player.getOutputNumber(), player.getHandFormatted()));
        });
        this.userThreads.add(thread);
        thread.start();
    }
}