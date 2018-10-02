package namesayer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import namesayer.recording.Name;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecordingFragmentController {

    @FXML private JFXSpinner spinner;
    @FXML private JFXButton recordButton;
    private Name name;

    public void initialize() {
        recordButton.setText("");
        spinner.setProgress(1);
    }

    void injectName(Name name) {
        this.name = name;
    }

    /**
     * Animates the recording button
     * @param mouseEvent
     */
    public void onRecordingButtonClicked(MouseEvent mouseEvent) {
        String temp = "Recording on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String recordingName = temp.replace(" ", "_");
        name.makeNewRecording(recordingName);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(spinner.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(3), new KeyValue(spinner.progressProperty(), 1)));
        timeline.play();
    }

}
