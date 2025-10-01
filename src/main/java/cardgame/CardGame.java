package cardgame;

import cardgame.io.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class CardGame {
    private final Scanner scan;
    private volatile CardDeck[] cardDecks;
    private List<Card> pack;
    private volatile AtomicBoolean isRunning;
    private volatile boolean isReady = false;
    private final List<Thread> userThreads;
    private final List<Player> players;

    public static void main(String[] args) {
        CardGame cardGame = new CardGame();
        cardGame.startGame();
    }

    public CardGame() {
        this.scan = new Scanner(System.in);
        this.userThreads = new ArrayList<>();
        this.pack = new ArrayList<>();
        this.players = new ArrayList<>();
        this.isRunning = new AtomicBoolean(false);
    }

    public void startGame() {
        int playerCount = this.getPlayerCount();
        this.cardDecks = new CardDeck[playerCount];

        loadPackFromFile();

        for (int i = 0; i < playerCount; ++i) {
            this.cardDecks[i] = new CardDeck();
        }

        for (int i = 0; i < playerCount; i++) {
            try {
                this.players.add(addPlayer(i, playerCount));
            } catch (InterruptedException e) {
                System.out.println("Error: Couldn't start player thread");
            }
        }

        //Give players cards
        boolean givenCards = false;
        while (!givenCards) {
            for (Player player : this.players) {
                givenCards = player.getNumberOfCards() == 4;
                if (!givenCards) {
                    player.pickupCard(this.pack.remove(0));
                }
            }
        }

        //Give Cards to Deck
        while (!this.pack.isEmpty()) {
            for (CardDeck deck : this.cardDecks) {
                if (!this.pack.isEmpty()) {
                    deck.addCard(this.pack.remove(0));
                }
            }
        }

        for (Thread thread : this.userThreads) {
            thread.start();
        }

        this.isReady = true;
        this.isRunning.set(true);


    }


    private void loadPackFromFile() {
        boolean isValid = false;
        while(!isValid) {
            System.out.println("Please enter location of pack to load:");
            this.scan.nextLine();
            String fileName = this.scan.nextLine();
            String path = "src/main/resources/packs/" + fileName;


            if(!FileManager.doesFileExist(path)) {
                System.out.println("Error: Pack File not Found");
                continue;
            }

            List<String> fileData = FileManager.readFile(path);

            if(fileData.size() % 8 != 0) {
                System.out.println("Error: Invalid Pack");
                continue;
            }

            try {
                for(String line : fileData) {
                    int number = Integer.parseInt(line);
                    if(number <= 0) {
                        throw new NumberFormatException();
                    }

                    Card card = new Card(number);
                    this.pack.add(card);
                }
                isValid = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Pack contains invalid values");
            }
        }
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

    private Player addPlayer(int playerNumber, int totalPlayers) throws InterruptedException {
        Player player = new Player(playerNumber);
        Thread thread = new Thread(() -> {
            List<String> outputFileArray = new ArrayList<>();
            while(!this.isReady) {
            }

            outputFileArray.add("player %d initial hand %s".formatted(player.getOutputNumber(), player.getHandFormatted()));

            while(this.isRunning.get()) {


                //Handle Player Won
                if (player.hasWon()) {
                    outputFileArray.add("player " + player.getOutputNumber() + " won");
                    synchronized (this) {
                        this.isRunning.set(false);
                        outputFileArray.add("Left because someone won");
                    }
                    break;
                }

                //Handle pickup
                Card newCard = this.cardDecks[playerNumber].getCard();
                if (newCard != null) {
                    player.pickupCard(newCard);
                    outputFileArray.add("player %d draws a %d from deck %d".formatted(player.getOutputNumber(), newCard.getNumber(), playerNumber + 1));
                } else {
                    continue; //Wait until you can pick up a card
                }

                //Handle Discard
                int discardDeckIndex = playerNumber == totalPlayers - 1 ? 0 : playerNumber + 1;
                Card oldCard = player.discardCard();
                this.cardDecks[discardDeckIndex].addCard(oldCard);
                outputFileArray.add("player %d discards a %d to deck %d".formatted(player.getOutputNumber(), oldCard.getNumber(), discardDeckIndex + 1));
                outputFileArray.add("player %d current hand is %s".formatted(player.getOutputNumber(), player.getHandFormatted()));


            }

            outputFileArray.add("player " + player.getOutputNumber() + " exits");
            outputFileArray.add("player %d hand: %s".formatted(player.getOutputNumber(), player.getHandFormatted()));
            System.out.println(outputFileArray);
        });
        this.userThreads.add(thread);
        return player;
    }
}