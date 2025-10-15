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
    private volatile AtomicBoolean isRunning = new AtomicBoolean(true);
    private volatile boolean isReady = false;
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
            Player player = new Player(i);
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
            executor.submit(runPlayer(this.players.get(i), playerCount));
        }


        this.isReady = true;
        countDownLatch.countDown();

        shutdownThreads();


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

    private Runnable runPlayer(Player player, int totalPlayers) {
        return () -> {
            int playerNumber = player.getNumber();
            List<String> outputFileArray = new ArrayList<>();


            outputFileArray.add("player %d initial hand %s".formatted(player.getOutputNumber(), player.getHandFormatted()));

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            while(CardGame.this.isRunning.get()) {



                try {
                    //Handle pickup
                    Card newCard = this.cardDecks[playerNumber].getCard(100, TimeUnit.MILLISECONDS);

                    if (newCard != null) {
                        player.pickupCard(newCard);
                        outputFileArray.add("player %d draws a %d from deck %d".formatted(player.getOutputNumber(), newCard.getNumber(), playerNumber + 1));

                        //Handle Discard
                        int discardDeckIndex = playerNumber == totalPlayers - 1 ? 0 : playerNumber + 1;
                        Card oldCard = player.discardCard();
                        this.cardDecks[discardDeckIndex].addCard(oldCard);
                        outputFileArray.add("player %d discards a %d to deck %d".formatted(player.getOutputNumber(), oldCard.getNumber(), discardDeckIndex + 1));
                        outputFileArray.add("player %d current hand is %s".formatted(player.getOutputNumber(), player.getHandFormatted()));

                    } else {
                        if(!CardGame.this.isRunning.get()) {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupt");
                    Thread.currentThread().interrupt();
                }

                //Handle Player Won
                if (player.hasWon()) {
                    outputFileArray.add("player " + player.getOutputNumber() + " won");
                    CardGame.this.isRunning.set(false);
                    outputFileArray.add("Left because someone won");
                    break;
                }



//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }


            }

            outputFileArray.add("player " + player.getOutputNumber() + " exits");
            outputFileArray.add("player %d hand: %s".formatted(player.getOutputNumber(), player.getHandFormatted()));
            System.out.println(outputFileArray.size());
        };
    }

    private void shutdownThreads() {

        this.executor.shutdown();
        try{
            if(!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

    }

    public void createPack(int numPlayers) {
        int totalCards = numPlayers * 8;

        List<Integer> pack = new ArrayList<Integer>();

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