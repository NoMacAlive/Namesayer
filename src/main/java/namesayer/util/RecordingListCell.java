package namesayer.util;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListCell;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import namesayer.RecordingScreenController;
import namesayer.recording.Recording;

import java.io.IOException;



/**
 * Represents a custom listCell which displays a delete button and dialog to prompt the user
 */
public class RecordingListCell extends JFXListCell<Recording> {


    @FXML private Label itemLabel;
    @FXML private HBox listItemHBox;
    @FXML private JFXButton deleteButton;
    private RecordingScreenController parent;
    private Recording recording;


    public RecordingListCell() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/RecordingListCell.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
            listItemHBox.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    deleteButton.setVisible(true);
                }
            });

            listItemHBox.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    deleteButton.setVisible(false);
                }
            });
            deleteButton.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void injectParent(RecordingScreenController parent) {
        this.parent = parent;
    }


    @Override
    public void updateItem(Recording item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        recording = item;
        if (empty) {
            setGraphic(null);
        } else {
            if (item == null) {
                itemLabel.setText("<null>");
            } else {
                itemLabel.setText(item.toString());
            }
            setGraphic(listItemHBox);
        }
    }


    @FXML
    public void onDeleteButtonClicked(MouseEvent mouseEvent) {
        JFXAlert alert = new JFXAlert((Stage) listItemHBox.getScene().getWindow());
        alert.initModality(Modality.NONE);
        alert.setOverlayClose(false);
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label("Are you sure you want to delete " + recording));
        layout.setBody(new Label("This recording will be deleted permanently. Would you like to continue?"));
        JFXButton closeButton = new JFXButton("No");
        JFXButton okButton = new JFXButton("Yes");
        closeButton.getStyleClass().add("dialog-accept");
        okButton.getStyleClass().add("dialog-accept");
        closeButton.setOnAction(event -> alert.hideWithAnimation());
        okButton.setOnAction(event -> {
            parent.getSelectedName().removeRecording(recording);
            parent.hidePlayer();
            alert.hideWithAnimation();
        });
        layout.setActions(closeButton, okButton);
        alert.setContent(layout);
        alert.show();
    }
}
