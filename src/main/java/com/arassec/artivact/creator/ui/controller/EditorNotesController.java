package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.ui.event.EditorEvent;
import com.arassec.artivact.creator.ui.event.EditorEventType;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditorNotesController extends BaseController implements ApplicationListener<EditorEvent> {

    private final ProjectService projectService;

    @FXML
    private HBox container;

    @FXML
    private AnchorPane imagePane;

    @FXML
    private TextArea notesArea;

    @FXML
    public void initialize() {
        var activeArtivact = projectService.getActiveArtivact();

        var pathToPreviewImage = activeArtivact.getProjectRoot().resolve(activeArtivact.getPreviewImage());

        try (var fileInputStream = new FileInputStream(pathToPreviewImage.toString())) {
            var previewImageView = createPreviewImageView(fileInputStream);
            previewImageView.setX(0);
            previewImageView.setY(0);
            imagePane.getChildren().add(previewImageView);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not create artivact preview!", e);
        }

        notesArea.prefWidthProperty().bind(container.widthProperty());
        notesArea.prefHeightProperty().bind(container.heightProperty());

        notesArea.setText(activeArtivact.getNotes());
        notesArea.textProperty().addListener((observableValue, oldValue, newValue) -> activeArtivact.setNotes(newValue));
    }

    @Override
    public void onApplicationEvent(EditorEvent event) {
        if (EditorEventType.UPDATE_NOTES.equals(event.getType())) {
            notesArea.setText(projectService.getActiveArtivact().getNotes());
        }
    }
}
