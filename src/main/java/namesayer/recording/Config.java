package namesayer.recording;

import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import namesayer.MenuScreenController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

/**
 * Defines constants which are mainly folder names
 */
public class Config {
    public static final Path CREATIONS_FOLDER = Paths.get("recordings");
    public static final Path TEMP_RECORDINGS = Paths.get("temp");
    public static final Path SAVED_RECORDINGS = Paths.get("saved");
    public static final String WAV_EXTENSION = ".wav";
    public static final Path RATINGS = Paths.get("ratings.txt");
    public static final Path DATA_BASE  = Paths.get("database");
    public static Label coinCounter = new Label("Coins Collected: 0");
    public static Integer counter = 0;
    public static Properties coinsCount= new Properties();


    public static void IncrementCoinCounter() throws IOException {
        counter++;
        coinCounter.setText("Coins Collected: "+counter);
//        MenuScreenController.getInstance().updateCoinCount(coinCounter.getText());
        coinsCount.setProperty("coin",counter.toString());
        coinsCount.store(new FileOutputStream(CREATIONS_FOLDER.resolve("CoinsCounter.txt").toAbsolutePath().toString()),"This is the property of the coins count"+new Date().toString());
    }

    public static void loadCoinsCountProperty() throws IOException {
        if(!new File(CREATIONS_FOLDER.resolve("CoinsCounter.txt").toAbsolutePath().toString()).exists()) {
           Files.createFile(new File(CREATIONS_FOLDER.resolve("CoinsCounter.txt").toAbsolutePath().toString()).toPath());
            //build a new property file
            OutputStream output = null;
            try {
                output = new FileOutputStream(CREATIONS_FOLDER.resolve("CoinsCounter.txt").toAbsolutePath().toString());
                coinsCount.setProperty("coin", "0");
                coinsCount.store(output, "This is the property of the coins count" + new Date().toString());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }else{
            try {
                InputStream input = new FileInputStream(CREATIONS_FOLDER.resolve("CoinsCounter.txt").toAbsolutePath().toString());
                coinsCount.load(input);
                counter = Integer.parseInt(coinsCount.getProperty("coin"));
                counter--;
                IncrementCoinCounter();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Integer getCoinCounter(){
        return counter;
    }
}
