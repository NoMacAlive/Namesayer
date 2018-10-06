package namesayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.util.Pair;
import namesayer.recording.Config;

import java.util.Optional;

import static namesayer.recording.Config.DATA_BASE;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Config.loadCoinsCountProperty();
        Parent root = FXMLLoader.load(getClass().getResource("/MenuScreen.fxml"));
        primaryStage.setTitle("Name Sayer");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
