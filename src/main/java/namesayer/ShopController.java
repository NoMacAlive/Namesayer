package namesayer;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import namesayer.recording.Config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class ShopController implements Initializable {
    public JFXButton coinsCollectedCounter;
    public JFXButton loadNewDataBaseButton;
    public Label coin20;
    public JFXButton buyBackground1;
    public ImageView buyBackground2;
    public Label coin50;
    public Label coin100;
    public JFXButton buyBackground4;
    public JFXButton buyBackground3;
    public JFXButton buyBackground5;
    public Label coin150;
    public AnchorPane background;

//    private FXMLLoader menuLoader;
//    private MenuScreenController mnController;

    private File bg1;
    private File bg2;
    private File bg3;
    private File bg4;
    private File bg5;


    public void onSelectAudioDatabaseFolder(MouseEvent mouseEvent) {
    }

    public void onBackButtonClicked(MouseEvent mouseEvent) {
//        Parent root = null;
//        try {
//            root = FXMLLoader.load(getClass().getResource("/MenuScreen.fxml"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Parent root = Config.menuRoot;
        Scene scene = buyBackground1.getScene();
        scene.setRoot(root);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coinsCollectedCounter.setText("Coins Collected: "+ Config.getCoinCounter());
        bg1 = new File("background1.jpg");
        bg2 = new File("background2.jpg");
        bg3 = new File("background3.jpg");
        bg4 = new File("background4.jpg");
        bg5 = new File("background5.jpg");
//        menuLoader = new FXMLLoader(getClass().getResource("MenuScreen.fxml"));
//        try {
//            Parent root = (Parent) menuLoader.load();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mnController = menuLoader.getController();
    }

    public void onBuyBackground1Clicked(MouseEvent mouseEvent) throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MenuScreen.fxml"));
//        Parent root = loader.load();
//        MenuScreenController controller = loader.getController();
        MenuScreenController.setBackground(1);
//        Scene scene = buyBackground1.getScene();
//        scene.setRoot(root);
        if(Config.counter>=20) {
            background.getStyleClass().clear();
            background.getStyleClass().add("background1");
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground2Clicked(MouseEvent mouseEvent) {
        Config.mnCtrl.background.getStyleClass().clear();
        Config.mnCtrl.background.getStyleClass().add("background2");

        if(Config.counter>=50) {
            background.getStyleClass().clear();
            background.getStyleClass().add("background2");
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground4Clicked(MouseEvent mouseEvent) {
        MenuScreenController.setBackground(4);
        if(Config.counter>=100) {
            background.getStyleClass().clear();
            background.getStyleClass().add("background4");
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground3Clicked(MouseEvent mouseEvent) {
        MenuScreenController.setBackground(3);
        if(Config.counter>=100) {
            background.getStyleClass().clear();
            background.getStyleClass().add("background3");
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground5Clicked(MouseEvent mouseEvent) {
        MenuScreenController.setBackground(5);
        if(Config.counter>=150) {
            background.getStyleClass().clear();
            background.getStyleClass().add("background5");
        }else{
            showCoinNotEnough();
        }
    }

    public void showCoinNotEnough(){
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Error");
        dialog.setContentText("NOT SUFFICIENT COIN");
        dialog.show();

    }
}
