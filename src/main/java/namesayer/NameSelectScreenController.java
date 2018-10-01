package namesayer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import namesayer.recording.Name;
import namesayer.recording.NameStorageManager;
import namesayer.util.EmptySelectionModel;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class NameSelectScreenController {

    public JFXButton searchingButton;
    @FXML private GridPane parentPane;
    @FXML private JFXButton nextButton;
    @FXML private JFXTextField nameSearchBar;
    @FXML private JFXListView<Name> nameListView;
    @FXML private JFXToggleButton randomToggle;
    private JFXSnackbar bar;

    private NameStorageManager nameStorageManager;
    private ObservableList<Name> listOfNames;
    private static boolean randomSelected = false;

    public void initialize() {
        nameStorageManager = NameStorageManager.getInstance();
        listOfNames = nameStorageManager.getNamesList();

        //Use custom ListCell with checkboxes
        nameListView.setCellFactory(value -> new JFXListCell<Name>() {
            JFXCheckBox checkBox = new JFXCheckBox();
            Name recycledName = null;

            @Override
            public void updateItem(Name item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    if (recycledName != null) {
                        checkBox.selectedProperty().unbindBidirectional(recycledName.selectedProperty());
                    }
                    checkBox.selectedProperty().bindBidirectional(item.selectedProperty());
                    recycledName = item;
                    setGraphic(checkBox);
                }
            }
        });
        nameListView.setSelectionModel(new EmptySelectionModel<>());
        nameListView.setItems(listOfNames);
        nameListView.setExpanded(false);
        bar = new JFXSnackbar(parentPane);
        bar.getStylesheets().addAll("/css/Material.css");
    }

    /**
     * Loads the RecordingScreen
     */
    public void onNextButtonClicked(MouseEvent mouseEvent) throws IOException {
        if (nameStorageManager.getSelectedNamesList().isEmpty()){
            bar.enqueue(new JFXSnackbar.SnackbarEvent("Please select a name first"));
            return;
        }
        Parent root = FXMLLoader.load(getClass().getResource("/RecordingScreen.fxml"));
        Scene scene = nameSearchBar.getScene();
        scene.setRoot(root);
    }

    public void setRandom() {
        if (randomToggle.isDisableAnimation()) {
            randomSelected = false;
        } else {
            randomSelected = true;
        }
    }

    public static boolean RandomToggleOn() {
        return randomSelected;
    }


    /**Refreshes the current list whenever the user types a key
     * This performs a search functionality
     */
    @FXML
    public void onSearchBarKeyTyped(KeyEvent keyEvent) {
        String userInput = nameSearchBar.getCharacters().toString().toLowerCase();
        if (userInput.isEmpty()) {
            listOfNames = nameStorageManager.getNamesList();
        } else {
            listOfNames = listOfNames.stream()
                                     .filter(name -> name.getName().toLowerCase().contains(userInput))
                                     .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
        //TODO change to bindings if u have time
        nameListView.setItems(listOfNames);
    }

    public void onBackButtonClicked(MouseEvent mouseEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/MenuScreen.fxml"));
            Scene scene = nameSearchBar.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onSelectAllButtonClicked(MouseEvent mouseEvent) {
        listOfNames.forEach(name -> name.setSelected(true));
    }

    public void onSelectNoneButtonClicked(MouseEvent mouseEvent) {
        listOfNames.forEach(name -> name.setSelected(false));
    }

    public void onSearchignButtonClicked(ActionEvent actionEvent) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Seaching Full Name");
        dialog.setHeaderText("Looking for a name with first name and second name:");

// Set the icon (must be included in the project).
//        dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString()));

// Set the button types.
        ButtonType loginButtonType = new ButtonType("Search", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the FirstName and LastName labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField FirstName = new TextField();
        FirstName.setPromptText("FirstName");
        PasswordField LastName = new PasswordField();
        LastName.setPromptText("LastName");

        grid.add(new Label("FirstName:"), 0, 0);
        grid.add(FirstName, 1, 0);
        grid.add(new Label("LastName:"), 0, 1);
        grid.add(LastName, 1, 1);

// Enable/Disable login button depending on whether a FirstName was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

// Do some validation (using the Java 8 lambda syntax).
        FirstName.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the FirstName field by default.
        Platform.runLater(() -> FirstName.requestFocus());

// Convert the result to a FirstName-LastName-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(FirstName.getText(), LastName.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(FirstNameLastName -> {
//            System.out.println("FirstName=" + FirstNameLastName.getKey() + ", LastName=" + FirstNameLastName.getValue());
            Name fusedName = nameStorageManager.fusingTwoNames(FirstNameLastName.getKey(),FirstNameLastName.getValue());
            if(fusedName.equals(null)){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning Dialog");
                alert.setHeaderText("WARNING!");
                alert.setContentText("There are no such names in the database!");

                alert.showAndWait();

            }
        });

    }
}
