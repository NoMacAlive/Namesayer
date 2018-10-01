package namesayer.recording;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static namesayer.recording.Config.RATINGS;
import static namesayer.recording.Config.SAVED_RECORDINGS;
import static namesayer.recording.Config.TEMP_RECORDINGS;
import static namesayer.recording.Config.WAV_EXTENSION;

public class Name implements Comparable<Name> {

    private String name;
    private Path directory;
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private Properties recordingRatingProperties = new Properties();

    //Store recordings as observables so we can bind them through viewmodel pattern
    private ObservableList<Recording> savedRecordings = FXCollections.observableArrayList();
    private ObservableList<Recording> tempRecordings = FXCollections.observableArrayList();

    public Name(String name, Path directory) {
        this.name = name;
        this.directory = directory;
        savedRecordings.addListener(new ListChangeListener<Recording>() {
            @Override
            public void onChanged(Change<? extends Recording> c) {
                refreshRatingFile();
            }
        });
    }

    //this constructor returns a name of the combination of the first name and the last name
    public Name(Name first,Name last){
//        temporarily like this!
        this(first.getName()+""+last.getName(),null);
    }

    /**
     * Syncs the rating properties with the file
     */
    private void refreshRatingFile() {
        try (OutputStream output = new FileOutputStream(directory.resolve(RATINGS).toAbsolutePath().toString())) {
            recordingRatingProperties.clear();
            for (Recording recording : savedRecordings) {
                recordingRatingProperties.setProperty(recording.toString(), recording.getRating() + "");
            }
            for (Recording recording : tempRecordings) {
                recordingRatingProperties.setProperty(recording.toString(), recording.getRating() + "");
            }
            recordingRatingProperties.store(output, "Ratings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a new recording using FFMPEG
     */
    public void makeNewRecording(String recordingName) {
        Thread thread = new Thread(() -> {
            Path newRecordingPath = directory.resolve(TEMP_RECORDINGS).resolve(recordingName + WAV_EXTENSION).toAbsolutePath();
            String command = "ffmpeg -loglevel \"quiet\" -f alsa -i default -t 3 -acodec pcm_s16le -ar 16000 -ac 1 -y \"" +
                    newRecordingPath.toString() + "\"";
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
            try {
                Process process = builder.start();
                process.waitFor();
                Platform.runLater(() -> tempRecordings.add(new Recording(newRecordingPath, true)));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void addSavedRecording(Recording recording) {
        savedRecordings.add(recording);
        recording.ratingProperty().addListener((observable, oldValue, newValue) -> {
            refreshRatingFile();
        });
    }

    public void removeRecording(Recording recording) {
        try {
            if (savedRecordings.contains(recording)) {
                Files.deleteIfExists(recording.getRecordingPath());
                savedRecordings.remove(recording);
            }
            if (tempRecordings.contains(recording)) {
                Files.deleteIfExists(recording.getRecordingPath());
                tempRecordings.remove(recording);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeTempRecordings() {
        for (Recording recording : tempRecordings) {
            removeRecording(recording);
        }
    }


    public ObservableList<Recording> getSavedRecordings() {
        return FXCollections.unmodifiableObservableList(savedRecordings);
    }

    public ObservableList<Recording> getTempRecordings() {
        return FXCollections.unmodifiableObservableList(tempRecordings);
    }


    public void saveTempRecordings() {
        tempRecordings.forEach(recording -> {
            Path newPath = directory.resolve(SAVED_RECORDINGS)
                                    .resolve(recording.getRecordingPath().getFileName());
            try {
                Files.copy(recording.getRecordingPath(), newPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recording.setRecordingPath(newPath);
            addSavedRecording(recording);
        });
        tempRecordings.clear();
    }

    public boolean getSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public String getName() {
        return name;
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Name name1 = (Name) o;

        return name.equals(name1.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Name o) {
        return this.getName().compareTo(o.getName());
    }
}
