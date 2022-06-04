package com.arassec.artivact.creator.ui.controller;


import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.ArtivactModel;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.ui.event.EditorEvent;
import com.arassec.artivact.creator.ui.event.EditorEventType;
import com.arassec.artivact.creator.ui.model.UploadModelUserData;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Slf4j
@Component
public class EditorContentModelController extends EditorContentBaseController {

    @FXML
    private FlowPane modelContentPane;

    public EditorContentModelController(ProjectService projectService, MessageSource messageSource) {
        super(projectService, messageSource);
    }

    @FXML
    public void initialize() {
        initialize(modelContentPane, EditorEventType.MODEL_SELECTED, "#editorContentModelPane");
    }

    @Override
    public void onApplicationEvent(EditorEvent event) {
        super.onApplicationEvent(event);
        if (EditorEventType.UPDATE_MODEL_CONTENT.equals(event.getType())) {
            updateContent(event.getIndex());
        }
    }

    @Override
    protected void updateContent(int index) {
        modelContentPane.getChildren().clear();

        var toggleUploadMenuItem = new MenuItem(
                messageSource.getMessage("editor.content.upload.toggle.menu-item", null, Locale.getDefault())
        );
        toggleUploadMenuItem.setOnAction(actionEvent -> {
            var source = ((MenuItem) actionEvent.getSource());
            var uploadModelUserData = (UploadModelUserData) source.getUserData();
            var model = uploadModelUserData.getModel();

            if (model.getVaultUploadFiles().contains(uploadModelUserData.getFilename())) {
                model.getVaultUploadFiles().remove(uploadModelUserData.getFilename());
            } else {
                model.getVaultUploadFiles().add(uploadModelUserData.getFilename());
            }

            projectService.saveArtivact(projectService.getActiveArtivact());
            updateContent(index);
        });

        var contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-selection-bar: lightgrey;");
        contextMenu.getItems().add(toggleUploadMenuItem);

        var artivactModel = projectService.getActiveArtivact().getModels().get(index);
        try (var inputStream = Files.list(projectService.getActiveArtivact().getProjectRoot().resolve(artivactModel.getPath()))) {
            inputStream.forEach(filePath -> {
                var assetPreviewPane = createPreview(artivactModel, filePath);

                assetPreviewPane.setOnContextMenuRequested(contextMenuEvent -> {
                    toggleUploadMenuItem.setUserData(new UploadModelUserData(artivactModel, filePath.getFileName().toString()));
                    contextMenu.show(assetPreviewPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                });

                modelContentPane.getChildren().add(assetPreviewPane);
            } );
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not read model directory!", e);
        }
    }

    private Pane createPreview(ArtivactModel artivactModel, Path filePath) {
        String[] fileEndingParts = filePath.toString().split("\\.");
        String fileEnding = fileEndingParts[fileEndingParts.length - 1];

        Pane preview;

        if ("blend".equals(fileEnding) || "blend1".equals(fileEnding)) {
            preview = createRawPreview(projectService.getActiveArtivact().getProjectRoot().resolve("Utils/blender-logo.png"),
                    false, false);
        } else if ("glb".equals(fileEnding) || "gltf".equals(fileEnding)) {
            preview = createRawPreview(projectService.getActiveArtivact().getProjectRoot().resolve("Utils/gltf-logo.png"),
                    false, false);
        } else {
            preview = createRawPreview(projectService.getActiveArtivact().getProjectRoot().resolve("Utils/fallback-image.png"),
                    false, false);
        }

        var horizontalBox = new HBox();

        var indexLabel = new Label(fileEnding);
        HBox.setMargin(indexLabel, new Insets(12, 15, 0, 0));
        horizontalBox.getChildren().add(indexLabel);

        var detailsSpacer = new Pane();
        HBox.setHgrow(detailsSpacer, Priority.ALWAYS);
        horizontalBox.getChildren().add(detailsSpacer);

        var uploadIcon = new FontIcon("fas-upload");
        uploadIcon.setIconSize(24);
        HBox.setMargin(uploadIcon, new Insets(5, 5, 0, 0));
        if (artivactModel.getVaultUploadFiles() != null
                && artivactModel.getVaultUploadFiles().contains(filePath.getFileName().toString())) {
            uploadIcon.setIconColor(Paint.valueOf("green"));
            addTooltip(uploadIcon, "editor.content.upload.true.tooltip");
        } else {
            uploadIcon.setIconColor(Paint.valueOf("lightgrey"));
            addTooltip(uploadIcon, "editor.content.upload.false.tooltip");
        }
        horizontalBox.getChildren().add(uploadIcon);

        ((VBox) preview.getChildren().get(0)).getChildren().add(horizontalBox);

        return preview;
    }

}
