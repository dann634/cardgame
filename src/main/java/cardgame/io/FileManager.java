package cardgame.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

        StringBuilder output = new StringBuilder();
        for(String line : data) {
            output.append(line).append("\n");
        }

        try (FileChannel channel = FileChannel.open(Paths.get("src/main/resources/output/" + path),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer buffer = ByteBuffer.wrap(output.toString().getBytes());
            channel.write(buffer);
            channel.force(true);  // Ensures durability, forces direct write to file
        }

        catch (IOException e) {
            System.out.println("Error: Writing File Failed");
        }


    }

    public static boolean doesFileExist(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }





}
