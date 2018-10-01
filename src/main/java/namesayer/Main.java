package namesayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static namesayer.recording.Config.DATA_BASE;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/MenuScreen.fxml"));
        primaryStage.setTitle("Name Sayer");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();
       System.out.println(DATA_BASE.toAbsolutePath());
    }


    public static void main(String[] args) {
        launch(args);
    }
}
