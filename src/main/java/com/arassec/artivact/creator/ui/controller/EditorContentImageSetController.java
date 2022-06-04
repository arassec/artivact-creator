package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.ArtivactImage;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.ui.event.EditorEventType;
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

import java.util.Locale;

@Slf4j
@Component
public class EditorContentImageSetController extends EditorContentBaseController {

    @FXML
    private FlowPane imageSetContentPane;

    public EditorContentImageSetController(ProjectService projectService, MessageSource messageSource) {
        super(projectService, messageSource);
    }

    @FXML
    public void initialize() {
        initialize(imageSetContentPane, EditorEventType.IMAGE_SET_SELECTED, "#editorContentImageSetPane");
    }

    protected void updateContent(int imageSetIndex) {
        imageSetContentPane.getChildren().clear();

        var toggleUploadMenuItem = new MenuItem(
                messageSource.getMessage("editor.content.upload.toggle.menu-item", null, Locale.getDefault())
        );
        toggleUploadMenuItem.setOnAction(actionEvent -> {
            var source = ((MenuItem) actionEvent.getSource());
            var artivactImage = (ArtivactImage) source.getUserData();
            artivactImage.setUploadToVault(!artivactImage.isUploadToVault());
            projectService.saveArtivact(projectService.getActiveArtivact());
            updateContent(imageSetIndex);
        });

        var contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-selection-bar: lightgrey;");
        contextMenu.getItems().add(toggleUploadMenuItem);

        projectService.getActiveArtivact().getImageSets().get(imageSetIndex).getImages().forEach(asset -> {
            var assetPreviewPane = createPreview(asset);

            assetPreviewPane.setOnContextMenuRequested(contextMenuEvent -> {
                toggleUploadMenuItem.setUserData(asset);
                contextMenu.show(assetPreviewPane, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            });

            imageSetContentPane.getChildren().add(assetPreviewPane);
        });
    }

    private Pane createPreview(ArtivactImage artivactImage) {
        Pane preview = createRawPreview(projectService.getActiveArtivact(), artivactImage);

        var horizontalBox = new HBox();

        var indexLabel = new Label(String.format("%03d", artivactImage.getNumber()));
        HBox.setMargin(indexLabel, new Insets(12, 0, 0, 0));
        horizontalBox.getChildren().add(indexLabel);

        var detailsSpacer = new Pane();
        HBox.setHgrow(detailsSpacer, Priority.ALWAYS);
        horizontalBox.getChildren().add(detailsSpacer);

        var uploadIcon = new FontIcon("fas-upload");
        uploadIcon.setIconSize(24);
        HBox.setMargin(uploadIcon, new Insets(5, 5, 0, 0));
        if (!artivactImage.isUploadToVault()) {
            uploadIcon.setIconColor(Paint.valueOf("lightgrey"));
            addTooltip(uploadIcon, "editor.content.upload.false.tooltip");
        } else {
            uploadIcon.setIconColor(Paint.valueOf("green"));
            addTooltip(uploadIcon, "editor.content.upload.true.tooltip");
        }
        horizontalBox.getChildren().add(uploadIcon);

        ((VBox) preview.getChildren().get(0)).getChildren().add(horizontalBox);

        return preview;
    }
}
