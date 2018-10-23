package namesayer.recording;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import static namesayer.recording.Config.*;

public class Name implements Comparable<Name> {

    private String name;
    private Path directory;
    private BooleanProperty selected = new SimpleBooleanProperty(false);
    private Properties recordingRatingProperties = new Properties();

    //Store recordings as observables so we can bind them through viewmodel pattern
    private ObservableList<Recording> savedRecordings = FXCollections.observableArrayList();
    private ObservableList<Recording> tempRecordings = FXCollections.observableArrayList();
    private boolean hasUserAttempt = false;

    public Name(String name, Path directory) {
        this.name = name;
        this.directory = directory;
        savedRecordings.addListener((ListChangeListener<Recording>) c -> refreshRatingFile());
    }

    //this constructor returns a name of the combination of the first name and the last name
    public Name(Name first,Name last) throws IOException {
//        temporarily like this!
        this(first.getName()+" "+last.getName(), Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName()));
        if (!Files.isDirectory(Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName()))) {
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName()));

            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ first.getName() + "_" + last.getName() + "/saved"));
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ first.getName() + "_" + last.getName() +"/temp"));
        }

        File[] filesFirstName = new File(CREATIONS_FOLDER + "/"+first.getName()  + "/saved").listFiles();
        File[] filesLastName = new File(CREATIONS_FOLDER + "/"+last.getName()  + "/saved").listFiles();
        
        String wavFile1 = filesFirstName[0].toString();
        String wavFile2 = filesLastName[0].toString();

        try {
            AudioInputStream clip1 = AudioSystem.getAudioInputStream(new File(wavFile1));
            AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File(wavFile2));

            AudioInputStream appendedFiles = new AudioInputStream(new SequenceInputStream(clip1, clip2), clip1.getFormat(), clip1.getFrameLength() + clip2.getFrameLength());
            
            File concat = new File(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName() + "/saved/" +first.getName() + "_" + last.getName() + ".wav");
            AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, concat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        savedRecordings.addListener(new ListChangeListener<Recording>() {
            @Override
            public void onChanged(Change<? extends Recording> c) {
                refreshRatingFile();
            }
        });
        
        this.directory = Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName() + "/saved/" + first.getName() + "_" + last.getName() + ".wav");
        
        Recording recording = new Recording(this.directory);
        addSavedRecording(recording); 
        
    }

    /**
     * This constructor concadenate multiple names and return a new concadenated name
     * @param names
     */
    public Name(List<Name> names) {
    	StringBuilder strbuilder = new StringBuilder();
    	for(Name name:names) {
    		strbuilder.append(name.getName()+"_");
    	}
    	strbuilder.deleteCharAt(strbuilder.length()-1);
    	this.name = strbuilder.toString();
    	if (!Files.isDirectory(Paths.get(CREATIONS_FOLDER + "/"+ name))) {
    		try {
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/" + name));
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ name + "/saved"));
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ name +"/temp"));
    		}catch(IOException e) {
    			e.printStackTrace();
    		}
        }
    	this.directory = Paths.get("recordings/"+this.name);
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
            String command = "ffmpeg -loglevel \"quiet\" -f alsa -i default -t 3 -y \"" + newRecordingPath.toString() + "\"";
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
            System.out.println(command);
            try {
                Process process = builder.start();
                process.waitFor();
                Platform.runLater(() -> tempRecordings.add(new Recording(newRecordingPath, true)));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        hasUserAttempt = true;
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
    
    public ObservableList<Recording> getSavedRecording(){
    	return savedRecordings; 
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
    
    public Recording getARecording() {
    	Recording randomRecording= null;
    	if(!savedRecordings.isEmpty()) {
    		randomRecording = savedRecordings.get(0);
    	}
    	return randomRecording;
    }

    /**
     * Returns a boolean indicate if the user has attempted this name
     * @return
     */
    public boolean isUserAttempted() {
        return hasUserAttempt;
    }

    /**
     * play two recordings sequentially, one is a recording from the database another one is the lastest user attempt
     */
    public void playAssess() {
        savedRecordings.get(0).playAudio(50);
        savedRecordings.get(savedRecordings.size()-1).playAudio(50);
    }
}
