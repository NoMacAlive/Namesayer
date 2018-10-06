package namesayer.recording;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import namesayer.MenuScreenController;
import namesayer.ShopController;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Defines constants which are mainly folder names
 */
public class Config implements Initializable {
    public static final Path CREATIONS_FOLDER = Paths.get("recordings");
    public static final Path TEMP_RECORDINGS = Paths.get("temp");
    public static final Path SAVED_RECORDINGS = Paths.get("saved");
    public static final String WAV_EXTENSION = ".wav";
    public static final Path RATINGS = Paths.get("ratings.txt");
    public static final Path DATA_BASE  = Paths.get("database");
    public static JFXButton coinCounter;
    public static Integer counter = 0;
    public static Properties coinsCount= new Properties();

    public static MenuScreenController mnCtrl;
    public static ShopController shopCtrl;
    public static Parent menuRoot;
    public static Parent shopRoot;

    public static void IncrementCoinCounter() throws IOException {
        counter++;
        coinCounter.setText("Coins Collected: "+counter);

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Integer getCoinCounter(){
        return counter;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader mnloader = new FXMLLoader(getClass().getResource("MenuScreen.fxml"));
        FXMLLoader shoploader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        try {
            menuRoot = mnloader.load();
            mnCtrl = mnloader.getController();
            shopRoot = shoploader.load();
            shopCtrl = shoploader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            loadCoinsCountProperty();
        } catch (IOException e) {
            System.out.println("Something went wrong");
        }
    }
}
