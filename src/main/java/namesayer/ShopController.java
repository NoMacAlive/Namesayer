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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import namesayer.recording.Config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    private File downloadDirectory = null;

    public void onSelectAudioDatabaseFolder(MouseEvent mouseEvent) {
    }

    public void onBackButtonClicked(MouseEvent mouseEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuScreen.fxml"));
            Scene scene = buyBackground1.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Paths.get("C:/Users/zhugu/Desktop"+"/"+bg1.toString()));
        System.out.println(bg1.isFile());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coinsCollectedCounter.setText("Coins Collected: "+ Config.getCoinCounter());
        bg1 = new File("src/background1.jpg");
        bg2 = new File("src/background2.jpg");
        bg3 = new File("src/background3.jpg");
        bg4 = new File("src/background4.jpg");
        bg5 = new File("src/background5.jpg");
//        menuLoader = new FXMLLoader(getClass().getResource("MenuScreen.fxml"));
//        try {
//            Parent root = (Parent) menuLoader.load();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mnController = menuLoader.getController();
    }

    public void onBuyBackground1Clicked(MouseEvent mouseEvent) throws IOException {
        if(Config.counter>=20) {
            if(downloadDirectory!=null){
                Files.copy(bg1.toPath(), Paths.get(downloadDirectory.toString()+"/"+bg1.toString().substring(4)), StandardCopyOption.REPLACE_EXISTING);
            }else{
                chooseADirectory();
            }
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground2Clicked(MouseEvent mouseEvent) throws IOException {
        if(Config.counter>=50) {
            if(downloadDirectory!=null){
                Files.copy(bg2.toPath(), Paths.get(downloadDirectory.toString()+"/"+bg2.toString().substring(4)), StandardCopyOption.REPLACE_EXISTING);
            }else{
                chooseADirectory();
            }
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground3Clicked(MouseEvent mouseEvent) throws IOException {
        if(Config.counter>=100) {
            if(downloadDirectory!=null){
                Files.copy(bg3.toPath(), Paths.get(downloadDirectory.toString()+"/"+bg3.toString().substring(4)), StandardCopyOption.REPLACE_EXISTING);
            }else{
                chooseADirectory();
            }
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground4Clicked(MouseEvent mouseEvent) throws IOException {
        if(Config.counter>=100) {
            if(downloadDirectory!=null){
                Files.copy(bg4.toPath(), Paths.get(downloadDirectory.toString()+"/"+bg4.toString().substring(4)), StandardCopyOption.REPLACE_EXISTING);
            }else{
                chooseADirectory();
            }
        }else{
            showCoinNotEnough();
        }
    }

    public void onBuyBackground5Clicked(MouseEvent mouseEvent) throws IOException {
        if(Config.counter>=150) {
            if(downloadDirectory!=null){
                Files.copy(bg5.toPath(), Paths.get(downloadDirectory.toString()+"/"+bg5.toString().substring(4)), StandardCopyOption.REPLACE_EXISTING);
            }else{
                chooseADirectory();
            }
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

    public void chooseADirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Please select the directory for downloaded wallpaper!");
        downloadDirectory = chooser.showDialog(buyBackground1.getScene().getWindow());
    }
}
