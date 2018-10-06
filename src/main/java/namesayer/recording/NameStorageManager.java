package namesayer.recording;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import namesayer.NameSelectScreenController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private List<Name> namesList = new LinkedList<>();


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

        } finally {

        }
    }


    /**
     * Create new database hierarchy
     */

    public void initialize(Path jkl,Button jk) throws IOException {
        Config.loadCoinsCountProperty();
        try {
            if (!Files.isDirectory(CREATIONS_FOLDER)) {
                Files.createDirectory(CREATIONS_FOLDER);
            } else {
                Files.walk(CREATIONS_FOLDER)
                     .map(Path::toFile)
                     .forEach(File::delete);
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
                             //TODO:initialise in loading method
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
                             newName.addSavedRecording(recording);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     });
                //sorts the final list
                Collections.sort(namesList);
//                Platform.runLater(() -> button.setDisable(false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private NameStorageManager() {
    }

    public void clear() {
        namesList.clear();
    }

    public void saveAllTempRecordings() {
        namesList.forEach(Name::saveTempRecordings);
    }

    public void removeAllTempRecordings() {
        namesList.forEach(Name::removeTempRecordings);
    }

    public ObservableList<Name> getNamesList() {
        return FXCollections.observableList(namesList);
    }

    public ObservableList<Name> getSelectedNamesList() {
        ObservableList<Name> list = namesList.stream()
                                             .filter(Name::getSelected)
                                             .collect(Collectors.toCollection(FXCollections::observableArrayList));
        if (NameSelectScreenController.RandomToggleOn()) {
            Collections.shuffle(list);
            return list;
        }
        return list;
    }

    //This method returns a Name with the firstName and lastName provided if any one of them doesn't exist in the database
//    return null
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

    public void addNewNametoList(Name newName){
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
    //
    public String[] parseNameFromString(String str){
        str.replace("-"," ");
        return str.split(" ");
    }

    //This method takes a list of strings and return a new Name object
    //the new Name object is a concadenation of the names in the list in the given order
    public Name fuseMultiNames(List<Name> names){
        //TODO: to be implemented
        return null;
    }

    //this method gives a Name list using the input List<String> as a reference
    //if there is a Name with a name equals one of the String given in the list then return it.
    public List<Name> getNameListForStrings(List<String> names){
        List<Name> output = new ArrayList<>();
        for(Name n:namesList){
            if(names.contains(n.getName())){
                output.add(n);
            }
        }
        return output;
    }
}
