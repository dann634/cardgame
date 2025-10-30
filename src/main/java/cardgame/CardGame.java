package cardgame;

import cardgame.io.FileManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CardGame {
    private final Scanner scan;
    private volatile CardDeck[] cardDecks;
    private final List<Card> pack;
    public static volatile AtomicBoolean isRunning;
    public static AtomicInteger winningPlayer;
    private final List<Player> players;
    public static CountDownLatch winningLatch;


    public static void main(String[] args) {
        CardGame cardGame = new CardGame();

        //Gets user input for number of players
        int playerCount = cardGame.getPlayerCount();
        cardGame.createPack(playerCount);

        //Loads pack file
        List<String> packData = cardGame.getPackFile();

        cardGame.startGame(playerCount, packData);
    }

    public CardGame() {
        this.scan = new Scanner(System.in);
        this.pack = new ArrayList<>();
        this.players = new ArrayList<>();

        isRunning = new AtomicBoolean(true);
        winningPlayer = new AtomicInteger(-1);
        winningLatch = new CountDownLatch(1);
    }

    public void startGame(int playerCount, List<String> packFile) {
        //Get Player Count from User and initialise dependent variables
        this.loadFileIntoPack(packFile);
        this.cardDecks = new CardDeck[playerCount];
        CountDownLatch countDownLatch = new CountDownLatch(playerCount);

        //Assign thread-safe deck's to array
        for (int i = 0; i < playerCount; ++i) {
            this.cardDecks[i] = new CardDeck();
        }

        //Give each player their decks to pickup from and discard to and create player objects
        for (int i = 0; i < playerCount; i++) {
            int discardDeckIndex = i == playerCount - 1 ? 0 : i + 1;
            CardDeck pickupDeck = this.cardDecks[i];
            CardDeck discardDeck = this.cardDecks[discardDeckIndex];

            Player player = new Player(i, pickupDeck, discardDeck, discardDeckIndex, countDownLatch);
            this.players.add(player);
        }


        //Give players cards in a round-robin fashion
        boolean givenCards = false;
        while (!givenCards) {
            for (Player player : this.players) {
                givenCards = player.getNumberOfCards() == 4;
                if (!givenCards) {
                    player.pickupCard(this.pack.remove(0));
                }
            }
        }

        //Fill decks with remaining cards from the pack
        while (!this.pack.isEmpty()) {
            for (CardDeck deck : this.cardDecks) {
                if (!this.pack.isEmpty()) {
                    deck.addCard(this.pack.remove(0));
                }
            }
        }

        //Start all player threads
        //They use a latch to start simultaneously
        for (int i = 0; i < playerCount; i++) {
            this.players.get(i).start();
        }


        // Wait for a player to win
        try {
            for (Player player : this.players) {
                player.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        //Update main game flag so remaining threads stop gracefully
        isRunning.set(false);


        for (int i = 0; i < this.cardDecks.length; i++) {
            CardDeck deck = this.cardDecks[i];
            ArrayList<String> outputArray = new ArrayList<>();
            outputArray.add(String.format("deck%d contents: %s", (i+1), deck.getOutputString()));
            FileManager.writeFile(String.format("deck%d_output.txt", (i+1)), outputArray);
        }
    }


    private List<String> getPackFile() {
        boolean isValid = false;
        while (!isValid) {
            System.out.println("Please enter location of pack to load:");
            this.scan.nextLine(); //Clears buffer from previous inputs
            String fileName = this.scan.nextLine();
            String path = "src/main/resources/packs/" + fileName;


            if (!FileManager.doesFileExist(path)) {
                System.out.println("Error: Pack File not Found");
            }

            List<String> fileData = FileManager.readFile(path);

            if(isPackValid(fileData)) {
                return fileData;
            }
        }
        return null;
    }

    public boolean isPackValid(List<String> fileData) {
        if (fileData.size() % 8 != 0) {
            return false;
        }

        for (String line : fileData) {
            int number = Integer.parseInt(line);
            if (number <= 0) {
                throw new NumberFormatException();
            }
        }
        return true;
    }

    private void loadFileIntoPack(List<String> fileData) {
        for(String line : fileData) {
            int number = Integer.parseInt(line);
            Card card = new Card(number);
            this.pack.add(card);
        }
    }


    private int getPlayerCount() {

        /*
        Takes user input from the console for the number of players
        Requirements:
        - Must be a number
        - Must be greater than 0
         */

        int players = 0;

        while (players <= 0) {
            try {
                System.out.println("Please enter the number of players");
                players = this.scan.nextInt();

                if(players <= 0) {
                    System.out.println("Error: Please enter a number greater than 0");
                }
            }

            catch (InputMismatchException e) { //If a non-numerical character is inputted
                System.out.println("Error: Please enter a non-negative integer");
                this.scan.nextLine();
            }

            catch (RuntimeException e) { //Catches every other possible run-time error
                System.out.println("Error: Please enter a non-negative integer");
            }
        }

        return players;
    }


    public void createPack(int numPlayers) {
        int totalCards = numPlayers * 8;

        List<Integer> pack = new ArrayList<>();

        for (int num = 1; num < numPlayers + 1; num++) {
            for (int i = 0; i < 4; i++) {
                pack.add(num);
            }
        }

        Random rand = new Random();
        int remaining = totalCards - pack.size();
        for (int i = 0; i < remaining; i++) {
            int randomNum = rand.nextInt(numPlayers) + (numPlayers + 1);
            pack.add(randomNum);
        }

        Collections.shuffle(pack);

        String folderPath = "src/main/resources/packs/";
        String fileName = "pack" + numPlayers + ".txt";

        try (FileWriter writer = new FileWriter(folderPath + fileName)) {
            for (int num : pack) {
                writer.write(num + "\n");
            }
            System.out.println("Pack created successfully!");
        } catch (IOException e) {
            System.err.println("Error writing the pack file: " + e.getMessage());
        }
    }

    public CardDeck[] getCardDecks() {
        return cardDecks;
    }

    public List<Player> getPlayers() {
        return players;
    }
}