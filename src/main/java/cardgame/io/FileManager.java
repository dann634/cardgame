package cardgame.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static List<String> readFile(String path) {

        //Reads a text file and returns the contents as a List

        ArrayList<String> fileContents = new ArrayList<>();
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(path));
            //Loop until the end of the file
            while((line = reader.readLine()) != null) {
                fileContents.add(line);
            }
            reader.close();
        }

        catch (FileNotFoundException e) {
            System.out.println("Error: Invalid File");
        }

        catch (IOException e) {
            System.out.println("Error: Reading File Failed");
        }

        return fileContents;
    }

    public static void writeFile(String path, List<String> data) {

        //Writes a list of String data to a specified path

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("./output/" + path));
            for(String line : data) {
                writer.write(line + "\n");
            }
            writer.close();
        }

        catch (IOException e) {
            System.out.println("Error: Writing to File Failed");
        }
    }

    public static boolean doesFileExist(String path) {
        return new File(path).exists();
    }





}
