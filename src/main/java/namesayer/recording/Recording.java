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
import java.nio.file.Paths;

public class Recording {

    private Path recordingPath;
    private boolean isCreatedByUser;

    //Property to store the rating which is binded to ratings file
    private DoubleProperty rating = new SimpleDoubleProperty(3.0);

    public void switchToNormalised(){
        recordingPath = Paths.get(this.getRecordingPath().toAbsolutePath().toString().substring(0,this.getRecordingPath().toAbsolutePath().toString().length()-4)+"1"+Config.WAV_EXTENSION);
    }

    public Recording(Path recordingPath) {
        this(recordingPath, false);
    }

    public Recording(Path recordingPath, boolean isCreatedByUser) {
        this.recordingPath = recordingPath;
        this.isCreatedByUser = isCreatedByUser;
    }

    //Play audio using bash command
    public void playAudio(int volume) {
        Thread thread = new Thread(() -> {
            String command = null;
            if(volume!=50) {
                command = "ffplay -nodisp -autoexit -loglevel quiet -volume " + volume + " \"" + recordingPath.toAbsolutePath().toString() + "\"";
            }else{
                command  = "ffplay -nodisp -autoexit -loglevel quiet \"" + recordingPath.toAbsolutePath().toString() + "\"";
            }
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
            System.out.println(command);
            try {
                Process process = builder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            Config.IncrementCoinCounter();
        }catch(Exception e){
//            e.printStackTrace();
        }
    }

    public double getRating() {
        return rating.get();
    }

    public Path getRecordingPath() {
        return recordingPath;
    }

    public void setRecordingPath(Path recordingPath) {
        this.recordingPath = recordingPath;
    }

    //Calculates the length
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Recording recording = (Recording) o;

        return recordingPath != null ? recordingPath.equals(recording.recordingPath) : recording.recordingPath == null;
    }

    @Override
    public int hashCode() {
        return recordingPath != null ? recordingPath.hashCode() : 0;
    }
}
