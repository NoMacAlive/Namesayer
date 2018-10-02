package namesayer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.effects.JFXDepthManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import namesayer.recording.Recording;
import org.controlsfx.control.Rating;

public class PlayerFragmentController {

    @FXML private JFXButton playButton;
    @FXML private JFXSpinner spinner;
    @FXML private GridPane playerCard;
    @FXML private Rating rating;
    private Recording recording;

    public void initialize() {
        JFXDepthManager.setDepth(playerCard, 1);
        playButton.setText("");
        spinner.setProgress(1);
    }

    void injectRecording(Recording recording) {
        this.recording = recording;
        rating.ratingProperty().bindBidirectional(recording.ratingProperty());
    }

    /**
     * Animates the play button
     * @param mouseEvent
     */
    @FXML
    public void onPlayButtonClicked(MouseEvent mouseEvent) {
        recording.playAudio();
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(spinner.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(recording.getLength()), new KeyValue(spinner.progressProperty(), 1)));
        timeline.play();
    }

}
