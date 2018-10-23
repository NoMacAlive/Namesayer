package namesayer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import namesayer.recording.Config;
import namesayer.recording.NameStorageManager;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuScreenController implements Initializable {

    @FXML public JFXProgressBar MicrophoneVolume;
    @FXML public ImageView MicrophoneButton;
    @FXML public JFXButton shopButton;
    public AnchorPane background;
    @FXML private JFXButton loadNewDataBaseButton;
    @FXML private JFXButton practiceButton;
    @FXML private JFXButton loadExistingDataBaseButton;
    @FXML private JFXButton coinsCollectedCounter;
    private boolean isFirstTimeClickMic = true;
    @FXML private ImageView microphoneTestingButton;
    public static MenuScreenController Instance = new MenuScreenController();
    private static int backGroundNumber = 0;

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        coinsCollectedCounter.setText("Coins Collected: "+Config.getCoinCounter().toString());
        String s = null;
    }

    public static MenuScreenController getInstance(){
        return Instance;
    }

    public void onPracticeModeClicked(MouseEvent mouseEvent) throws IOException, InterruptedException {
//        the progress indicator stage is based on code on
//        https://blog.csdn.net/wingfourever/article/details/8500619#
        HBox mHbox = new HBox(10);
        ProgressIndicator Bar = new ProgressIndicator(-1);
        Bar.setMaxSize(150, 150);
        Label Label = new Label("Loading DataBase...");
        Label.setFont(new Font(10));
        mHbox.getChildren().add(Bar);
        mHbox.getChildren().add(Label);
        Region veil = new Region();
        StackPane root1 = new StackPane();
        root1.getChildren().addAll(veil, Bar, Label);
        Scene scene1 = new Scene(root1, 300, 250);
        Window stage = new Stage();
        ((Stage) stage).setTitle("Loading DataBase");
        ((Stage) stage).setScene(scene1);
        ((Stage) stage).show();

        Task<Void> progressTask = new Task<Void>(){
            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("Succeeded");
                ((Stage) stage).close();
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                updateMessage("Cancelled");
            }

            @Override
            protected void failed() {
                super.failed();
                updateMessage("Failed");
            }

            @Override
            protected Void call() throws Exception {
                updateMessage("Finish");
                return null;
            }
        };
        veil.visibleProperty().bind(progressTask.runningProperty());
        Bar.progressProperty().bind(progressTask.progressProperty());
        Label.textProperty().bind(progressTask.messageProperty());

        Thread thread1 = new Thread(progressTask);
               thread1.start();
               thread1.join();
        Scene scene = practiceButton.getScene();
        Parent root = FXMLLoader.load(getClass().getResource("/NameSelectScreen.fxml"));
        scene.setRoot(root);

    }

    //reveal the progress bar after microphone button being clicked
    public void onMicrophoneButtonClicked() {
        if (isFirstTimeClickMic) {
            MicrophoneVolume.setVisible(true);
            Thread thread = new Thread(() -> {
                testMicrophone();
            });
            thread.start();
            isFirstTimeClickMic = !isFirstTimeClickMic;

        } else {
            MicrophoneVolume.setVisible(false);
            isFirstTimeClickMic = !isFirstTimeClickMic;
        }
    }

    /**
     * Set the microphone level using a moving average from TargetLine buffer
     */
    private void testMicrophone() {
        try {
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open();
            microphone.start();

            double highestVolume = 0;
            byte tempBuffer[] = new byte[1000];
            while (!isFirstTimeClickMic) {
                if (microphone.read(tempBuffer, 0, tempBuffer.length) > 0) {
                    double sumVolume = 0;
                    for (byte aTempBuffer : tempBuffer) {
                        double absoluteVolume = Math.abs(aTempBuffer);
                        sumVolume = sumVolume + absoluteVolume;
                        if (absoluteVolume > highestVolume) {
                            highestVolume = absoluteVolume;
                        }
                    }
                    double volume = (sumVolume / tempBuffer.length) / highestVolume;
                    Platform.runLater(() -> MicrophoneVolume.setProgress(volume));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //loadAnother Database
    public void onSelectLoadPreviousFolder(MouseEvent mouseEvent) throws IOException {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setTitle("Select the another database for your names");
        File selectedDirectory = chooser.showDialog(practiceButton.getScene().getWindow());
        if (selectedDirectory != null) {
            NameStorageManager storageManager = NameStorageManager.getInstance();
            storageManager.clear();
            storageManager.loadExistingHierarchy(selectedDirectory.toPath(), practiceButton);
            loadNewDataBaseButton.setDisable(true);
            loadExistingDataBaseButton.setDisable(true);
        }
    }

    public void onSelectAudioDatabaseFolder(MouseEvent mouseEvent) {
    }

    public void onShopClicked(MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Shop.fxml"));
        shopButton.getScene().setRoot(root);
    }

    public static void setBackground(int i){
        backGroundNumber = i;
    }

    public void updateCoinCount(String txt){
        coinsCollectedCounter.setText(txt);
    }

    public void onHelpPressed(MouseEvent mouseEvent) {
        if (Desktop.isDesktopSupported()) {
            try {
                File myFile = new File("HELP.pdf");
                Desktop.getDesktop().open(myFile);
            } catch (IOException ex) {
                // no application registered for PDFs
            }
        }
    }

}
