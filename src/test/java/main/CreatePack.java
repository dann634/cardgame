package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CreatePack {
    public static void main(String[] args) {
        int numPlayers = 100;
        createPack(numPlayers);
    }

    public static void createPack(int numPlayers) {
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

        try (FileWriter writer = new FileWriter(fileName)) {
            for (int num : pack) {
                writer.write(num + "\n");
            }
            System.out.println("Pack created successfully!");
        } catch (IOException e) {
            System.err.println("Error writing the pack file: " + e.getMessage());
        }
    }
}