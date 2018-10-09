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
    public Name(Name first,Name last) throws IOException {
//        temporarily like this!
        this(first.getName()+" "+last.getName(), Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName()));
        if (!Files.isDirectory(Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName()))) {
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName()));
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ first.getName() + "_" + last.getName() + "/saved"));
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ first.getName() + "_" + last.getName() +"/temp"));
        }


        System.out.println(first.getName());

        String wavFile1 = Paths.get(first.getSavedRecordings().get(0).getRecordingPath().toString()).toString();
        String wavFile2 = Paths.get(last.getSavedRecordings().get(0).getRecordingPath().toString()).toString();

        try {
            AudioInputStream clip1 = AudioSystem.getAudioInputStream(new File(wavFile1));
            AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File(wavFile2));

            AudioInputStream appendedFiles = new AudioInputStream(new SequenceInputStream(clip1, clip2), clip1.getFormat(), clip1.getFrameLength() + clip2.getFrameLength());
            
            File concat = new File(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName() + "/saved/" +first.getName() + "_" + last.getName() + ".wav");
            AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, concat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.directory = Paths.get(CREATIONS_FOLDER + "/"+first.getName() + "_" + last.getName());
    }
    
    //This constructor takes a list of names and returns concatenated version
    public Name(List<Name> names) throws IOException {
    	
    	String directoryName = names.get(0).toString();
    	
    	for(int i=1; i<names.size(); i++) {
    		directoryName = directoryName + "_" + names.get(i).toString(); 
    	}
    	
    	//Create new directory
    	if (!Files.isDirectory(Paths.get(CREATIONS_FOLDER + "/"+ directoryName))) {
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ directoryName));
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ directoryName + "/saved"));
            Files.createDirectory(Paths.get(CREATIONS_FOLDER + "/"+ directoryName +"/temp"));
        }
    	
    	//Creating a temp file to recursively add recordings
    	File temp = new File(CREATIONS_FOLDER + "/"+ directoryName + "/saved/" + directoryName + ".wav");
    	
    	String wavFile1 = Paths.get(names.get(0).getSavedRecordings().get(0).getRecordingPath().toString()).toString();
        String wavFile2 = Paths.get(names.get(1).getSavedRecordings().get(0).getRecordingPath().toString()).toString();
        try {
            AudioInputStream clip1 = AudioSystem.getAudioInputStream(new File(wavFile1));
            AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File(wavFile2));

            AudioInputStream appendedFiles = new AudioInputStream(new SequenceInputStream(clip1, clip2), clip1.getFormat(), clip1.getFrameLength() + clip2.getFrameLength());
            
            
            AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, temp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //Add each name to the temp recording
        if (names.size() > 2) { 
        	for(int i=2; i<names.size(); i++) {
        		
                String toAdd = Paths.get(names.get(1).getSavedRecordings().get(0).getRecordingPath().toString()).toString();
                try {
                    AudioInputStream clip1 = AudioSystem.getAudioInputStream(temp);
                    AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File(toAdd));

                    AudioInputStream appendedFiles = new AudioInputStream(new SequenceInputStream(clip1, clip2), clip1.getFormat(), clip1.getFrameLength() + clip2.getFrameLength());
                    
                   
                    AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, temp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        		

        	}
        }
        savedRecordings.add(new Recording(temp.toPath(),true));
        this.name = directoryName; 
        this.directory = Paths.get(CREATIONS_FOLDER + "/"+ directoryName);
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
