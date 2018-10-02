package namesayer;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import namesayer.recording.Name;
import namesayer.recording.NameStorageManager;
import namesayer.recording.Recording;
import namesayer.util.RecordingListCell;

import java.io.IOException;


public class RecordingScreenController {
    @FXML private GridPane parentPane;
    @FXML private JFXListView<Name> selectedNamesListView;
    @FXML private JFXListView<Recording> savedRecordingListView;
    @FXML private JFXListView<Recording> newRecordingListView;
    @FXML private HBox actionViewContainer;
    private JFXSnackbar bar;

    private NameStorageManager storageManager = NameStorageManager.getInstance();
    private ObservableList<Name> names;

    private Name selectedName;


    public void initialize() {
        names = storageManager.getSelectedNamesList();

        //custom listCell with nice icons
        selectedNamesListView.setCellFactory(param -> new JFXListCell<Name>() {
            @Override
            public void updateItem(Name item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    MaterialIconView icon = new MaterialIconView(MaterialIcon.PERSON);
                    icon.setFill(Paint.valueOf("#757575"));
                    setGraphic(icon);
                }
            }
        });
        //use custom Recording Listcells which support delete functionality
        savedRecordingListView.setCellFactory(param -> createRecordingCell());
        newRecordingListView.setCellFactory(param -> createRecordingCell());
        selectedNamesListView.setItems(names);
        bar = new JFXSnackbar(parentPane);
        bar.getStylesheets().addAll("/css/Material.css");
    }

    private RecordingListCell createRecordingCell() {
        RecordingListCell cell = new RecordingListCell();
        cell.injectParent(this);
        return cell;
    }

    @FXML
    public void onSelectedNameClicked(MouseEvent mouseEvent) {
        selectedName = selectedNamesListView.getSelectionModel().getSelectedItem();
        if (selectedName != null) {
            savedRecordingListView.setItems(selectedName.getSavedRecordings());
            newRecordingListView.setItems(selectedName.getTempRecordings());
        }
    }


    public void onSavedRecordingClicked(MouseEvent mouseEvent) {
        Recording selected = savedRecordingListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            displayPlayerFragment(selected);
        }
    }

    public void onNewRecordingClicked(MouseEvent mouseEvent) {
        Recording selected = newRecordingListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            displayPlayerFragment(selected);
        }
    }

    /**
     * Show the playerFragment
     *
     * @param recording
     */
    public void displayPlayerFragment(Recording recording) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PlayerFragment.fxml"));
            Parent root = loader.load();
            PlayerFragmentController controller = loader.getController();
            controller.injectRecording(recording);
            actionViewContainer.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves temp recordings into saved list
     *
     * @param mouseEvent
     */
    public void onSaveButtonClicked(MouseEvent mouseEvent) {
        if (selectedName != null) {
            if (selectedName.getTempRecordings().isEmpty()) {
                bar.enqueue(new JFXSnackbar.SnackbarEvent("No new recordings have been created"));
                return;
            }
            selectedName.saveTempRecordings();
            bar.enqueue(new JFXSnackbar.SnackbarEvent("Recordings are now saved"));
        } else {
            bar.enqueue(new JFXSnackbar.SnackbarEvent("Please select a name first"));
        }
    }

    /**
     * Allows user to make a new recording
     *
     * @param mouseEvent
     * @throws IOException
     */
    public void onNewButtonClicked(MouseEvent mouseEvent) throws IOException {
        if (selectedName != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/RecordingFragment.fxml"));
            Parent root = loader.load();
            RecordingFragmentController controller = loader.getController();
            controller.injectName(selectedName);
            actionViewContainer.getChildren().setAll(root);
        } else {
            bar.enqueue(new JFXSnackbar.SnackbarEvent("Please select a name first"));
        }
    }


    /**
     * Prompts the user to save all recordings before going back a screen
     *
     * @param mouseEvent
     */
    public void onBackButtonClicked(MouseEvent mouseEvent) {
        JFXAlert alert = new JFXAlert((Stage) parentPane.getScene().getWindow());
        alert.initModality(Modality.NONE);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label("Do you want to save your changes?"));
        layout.setBody(new Label("Would you like to save all temporary recordings? " +
                "Selecting no will cause them to be deleted"));
        JFXButton closeButton = new JFXButton("No");
        JFXButton okButton = new JFXButton("Yes");
        closeButton.getStyleClass().add("dialog-accept");
        okButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> {
            storageManager.removeAllTempRecordings();
            alert.hideWithAnimation();
            previousScreen();
        });
        okButton.setOnAction(event -> {
            storageManager.saveAllTempRecordings();
            alert.hideWithAnimation();
            previousScreen();
        });
        layout.setActions(closeButton, okButton);
        alert.setContent(layout);
        alert.show();
    }

    private void previousScreen() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/NameSelectScreen.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = selectedNamesListView.getScene();
        scene.setRoot(root);
    }

    public Name getSelectedName() {
        return selectedName;
    }

    public void hidePlayer() {
        actionViewContainer.getChildren().clear();
    }
}
