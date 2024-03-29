package namesayer.recording;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import namesayer.NameSelectScreenController;
import namesayer.util.MergeTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static namesayer.recording.Config.*;

public class NameStorageManager{

    private static final Pattern REGEX_NAME_PARSER = Pattern.compile("[a-zA-Z]+(?:\\.wav)");
    private static NameStorageManager instance = null;
    private static List<String> nameStringList = new ArrayList<>();
    private static List<Recording> AllRecordingsFromDataBase = new ArrayList<>();

    private static List<Name> namesList = new LinkedList<>();
    private Map<String,Name> userAttempts = new HashMap<>();

    /**
     * The use of singleton pattern to ensure that every namestoragemanager is the same one that holds the same database
     * @return NameStorageManager
     */
    public static NameStorageManager getInstance() {
        if (instance == null) {
            instance = new NameStorageManager();
        }
        return instance;
    }

    /**
     * load existing database hierarchy
     */
    public void loadExistingHierarchy(Path folderPath, Button button) throws IOException {
        Config.loadCoinsCountProperty();
        namesList = new LinkedList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path e : stream) {
                Name temp = new Name(e.getFileName().toString(), e);
                namesList.add(temp);
                nameStringList.add(temp.getName());
                Properties ratingProperties = new Properties();

                //load the properties
                ratingProperties.load(new FileInputStream(e.resolve(RATINGS).toFile()));
                try (DirectoryStream<Path> stream1 = Files.newDirectoryStream(e.resolve(SAVED_RECORDINGS))) {
                    for (Path p : stream1) {
                        Recording recording = new Recording(p);
                        double rating = Double.valueOf(ratingProperties.getProperty(recording.toString()));
                        recording.setRating(rating);
                        temp.addSavedRecording(recording);//add saved recordings to corresponding name
                    }

                } catch (IOException e1) {

                }
                try (DirectoryStream<Path> stream1 = Files.newDirectoryStream(new File(e.toString() + "/temp").toPath())) {
                    for (Path p : stream1) {
                        temp.addSavedRecording(new Recording(p, true));//add user created recordings to corresponding name
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Collections.sort(namesList);
                Platform.runLater(() -> button.setDisable(false));
            }
        } catch (IOException e) {

        } 
    }


    /**
     * Create new database hierarchy
     */
    public void initialize() throws IOException, InterruptedException {
//        Config.loadCoinsCountProperty();
        try {
            if (!Files.isDirectory(CREATIONS_FOLDER)) {
                Files.createDirectory(CREATIONS_FOLDER);
            } else {
            	Config.deleteAllFiles(CREATIONS_FOLDER.toFile()); //deleting the existing one and build another empty folder
            	Files.createDirectory(CREATIONS_FOLDER);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Ensure non-blocking
        Thread thread = new Thread(() -> {
            try (Stream<Path> paths = Files.walk(DATA_BASE)) {
                Map<String, Name> initializedNames = new HashMap<>();
                paths.filter(Files::isRegularFile)
                     .forEach(path -> {
                         //Extract name from provided database
                         Matcher matcher = REGEX_NAME_PARSER.matcher(path.getFileName().toString());
                         String name = "unrecognized";
                         if (matcher.find()) {
                             name = matcher.group(0).replace(".wav", "").toLowerCase();
                             nameStringList.add(name);
                             if (!name.isEmpty()) {
                                 name = name.substring(0, 1).toUpperCase() + name.substring(1);
                             }
                         }
                         Path nameFolder = CREATIONS_FOLDER.resolve(name);
                         Path tempFolder = nameFolder.resolve(TEMP_RECORDINGS);
                         Path savedFolder = nameFolder.resolve(SAVED_RECORDINGS);
                         Name newName = new Name(name, nameFolder);
                         try {
                             //Create sub-folders if not already created
                             if (!initializedNames.containsKey(name)) {
                                 Files.createDirectories(savedFolder);
                                 Files.createDirectories(tempFolder);
                                 initializedNames.put(name, newName);
                                 namesList.add(newName);
                             } else {
                                 newName = initializedNames.get(name);
                             }
                             Path recordingPath = savedFolder.resolve(path.getFileName());
                             Files.copy(path, recordingPath);
                             if (Files.notExists(nameFolder.resolve(RATINGS))) {
                                 Files.createFile(nameFolder.resolve(RATINGS));
                             }
                             Recording recording = new Recording(recordingPath);
                             AllRecordingsFromDataBase.add(recording);
                             newName.addSavedRecording(recording);
                         } catch (IOException e) {
                             //e.printStackTrace();
                         }
                     });
                //sorts the final list
                Collections.sort(namesList);
//                Platform.runLater(() -> button.setDisable(false));
            } catch (IOException e) {
               // e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        Task<Integer> normalising = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                for(Recording r:AllRecordingsFromDataBase) {
                    String cmd = "ffmpeg -hide_banner -i " + "\""+r.getRecordingPath().toAbsolutePath().toString()+"\"" + " -af silenceremove=start_threshold=-35dB:start_threshold=-35dB:stop_duration=1:stop_periods=1:start_periods=1 "+"\"" + r.getRecordingPath().toAbsolutePath().toString().substring(0,r.getRecordingPath().toAbsolutePath().toString().length()-4)+"1"+Config.WAV_EXTENSION+"\"";
                    r.switchToNormalised();
                    ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", cmd);
                    try {
                        Process process = builder.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e){

                    }
                }
                return null;
            }
        };
        Thread normalisingThread = new Thread(normalising);
        normalisingThread.setDaemon(true);
        normalisingThread.start();
    }

    private NameStorageManager() {
    }

    public void clear() {
        namesList.clear();
    }

    /**
     * save all the user attempts recordings to the recording folder
     */
    public void saveAllTempRecordings() { namesList.forEach(Name::saveTempRecordings); }

    /**
     * user decides to delete the attempts
     */
    public void removeAllTempRecordings() {
        namesList.forEach(Name::removeTempRecordings);
    }

    /**
     * returns a names list
     * @return List<Name></>
     */
    public ObservableList<Name> getNamesList() {
        return FXCollections.observableList(namesList);
    }


    /**
     * Returns a list of name that is selected in the NameSelectingScreen
     * @return List<Name></>
     */
    public ObservableList<Name> getSelectedNamesList() {
        ObservableList<Name> list = namesList.stream()
                                             .filter(Name::getSelected)
                                             .collect(Collectors.toCollection(FXCollections::observableArrayList));
        if (NameSelectScreenController.RandomToggleOn()) {
            Collections.shuffle(list);   //randomise the list if random toggle is on
            return list;
        }
        return list;
    }

    //This method returns a Name with the firstName and lastName provided if any one of them doesn't exist in the database
//    return null
    //this constructor is for testing
    public Name fusingTwoNames(String firstName,String lastName) throws IOException {
        Name first=null;
        Name last=null;
        for(Name a:namesList){
            if(a.getName().toLowerCase().equals(firstName)){
                first = a;
            }else if(a.getName().toLowerCase().equals(lastName)){
                last = a;
            }
        }
        if(first==null||last==null) {
            return null;
        }
        return new Name(first,last);
    }

    /**
     * add a new name at the beggining of the list
     * @param newName
     */
    public static void addNewNametoList(Name newName){
        namesList.add(0,newName);
    }
    public void setNameList(List<Name> list){ namesList = list; }
    public boolean isNameExistInDataBase(String s){
            if(nameStringList.contains(s)){
                return true;
            }
            return false;
    }

    //this method takes a string as input which is one of the name the user wish to practice
    public String[] parseNameFromString(String str){
        String st = str.replace('-',' ');
        return st.split("\\s");
    }

    //This method takes a list of strings and return a new Name object
    //the new Name object is a concatenation of the names in the list in the given order
    public void fuseMultiNames(List<Name> names) throws IOException {
    	for(Name n:names) {
    		System.out.println(n.getName());
    	}
    	
    	MergeTask merge = new MergeTask(names);
    	Thread th = new Thread(merge);
		th.start();
    }

    //this method gives a Name list using the input List<String> as a reference
    //if there is a Name with a name equals one of the String given in the list then return it.
    public List<Name> getNameListForStrings(List<String> names){
        for(String s:names){
            s=s.toLowerCase();
        }
        List<Name> output = new ArrayList<>();
        Map<Integer,Name> map = new HashMap<>();
        for(Name n:namesList){
            if(names.contains(n.getName().toLowerCase())){
                if(!output.contains(n)) {
                    map.put(names.indexOf(n.getName().toLowerCase()),n);

                }
            }
        }
        for(int i=0;i<map.size();i++){
            output.add(map.get(i));
        }
        return output;
    }

}
