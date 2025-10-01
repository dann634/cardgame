import java.util.Scanner;

public class CardGame {

    public static void main(String[] args) {

    }

    private final Scanner scan;
    private volatile CardDeck[] cardDecks;

    private volatile boolean isRunning;

    public CardGame() {
        this.scan = new Scanner(System.in);
    }

    public void startGame() {

        int playerCount = getPlayerCount(); //Gets input from user
        this.cardDecks = new CardDeck[playerCount];
        this.isRunning = false; //False so all threads



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

    private void addPlayer() {

    }

}


