package namesayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import namesayer.recording.NameStorageManager;

import java.io.File;
import java.nio.file.Files;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/MenuScreen.fxml"));
        primaryStage.setTitle("Name Sayer");
        primaryStage.setScene(new Scene(root, 1152, 648));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
