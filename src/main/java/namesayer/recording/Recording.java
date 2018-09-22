package namesayer.recording;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;

public class Recording {

    private Path recordingPath;
    private boolean isCreatedByUser;
    private DoubleProperty rating = new SimpleDoubleProperty(0.0);


    //This needs to be here to prevent garbage collection
    private MediaPlayer player;

    public Recording(Path recordingPath) {
        this(recordingPath, false);
    }

    public Recording(Path recordingPath, boolean isCreatedByUser) {
        this.recordingPath = recordingPath;
        this.isCreatedByUser = isCreatedByUser;
    }

    public void playAudio() {
        Thread thread = new Thread(() -> {
            String command = "ffplay -nodisp -autoexit -loglevel quiet \"" + recordingPath.toAbsolutePath().toString() + "\"";
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
            try {
                Process process = builder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public double getRating() {
        return rating.get();
    }

    public double getLength() {
        double durationInSeconds = 0.0;
        try{
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(recordingPath.toFile());
            AudioFormat format = audioInputStream.getFormat();
            long frames = audioInputStream.getFrameLength();
            durationInSeconds = (frames+0.0) / format.getFrameRate();

        } catch (UnsupportedAudioFileException|IOException e){
            e.printStackTrace();
        }
        return durationInSeconds;
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating.set(rating);
    }

    @Override
    public String toString() {
        return recordingPath.getFileName()
                            .toString()
                            .replace(".wav", "");
    }


}