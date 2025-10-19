package cardgame;

import cardgame.io.FileManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CardGame {
    private final Scanner scan;
    private volatile CardDeck[] cardDecks;
    private final List<Card> pack;
    public static final AtomicBoolean isRunning = new AtomicBoolean(true);
    private final List<Player> players;
    private CountDownLatch countDownLatch;
    private ExecutorService executor;

    public static void main(String[] args) {
        CardGame cardGame = new CardGame();
        cardGame.startGame();
    }

    public CardGame() {
        this.scan = new Scanner(System.in);
        this.pack = new ArrayList<>();
        this.players = new ArrayList<>();
    }

    public void startGame() {
        int playerCount = this.getPlayerCount();
        createPack(playerCount);
        this.cardDecks = new CardDeck[playerCount];
        this.executor = Executors.newFixedThreadPool(playerCount);
        this.countDownLatch = new CountDownLatch(1);



        loadPackFromFile();

        for (int i = 0; i < playerCount; ++i) {
            this.cardDecks[i] = new CardDeck();
        }

        for (int i = 0; i < playerCount; i++) {

            int discardDeckIndex = i == playerCount - 1 ? 0 : i + 1;
            CardDeck pickupDeck = this.cardDecks[i];
            CardDeck discardDeck = this.cardDecks[discardDeckIndex];

            Player player = new Player(i, pickupDeck, discardDeck, discardDeckIndex);
            this.players.add(player);
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
        for (int i = 0; i < playerCount; i++) {
            this.players.get(i).start();
        }

        boolean isFinished = false;
        int playerNumberFinished = -1;
        while(!isFinished) {
            for(Player player : this.players) {
                if(player.isAlive()) continue;
                isFinished = true;
                playerNumberFinished = player.getNumber() + 1;
                break;
            }
        }

        isRunning.set(false);
        for(Player player : this.players) {
            if(player.isAlive()) {
                player.interruptGame(playerNumberFinished);
            }
        }

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        for(Player player : this.players) {
            System.out.println(player.getActions());
        }


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
        File folder = new File(folderPath);
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

}