package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.ui.event.EditorEventType;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class EditorContentImageSetOverviewController extends EditorContentBaseController {

    @FXML
    private FlowPane imageSetOverviewContentPane;

    public EditorContentImageSetOverviewController(ProjectService projectService, MessageSource messageSource) {
        super(projectService, messageSource);
    }

    @FXML
    public void initialize() {
        initialize(imageSetOverviewContentPane, EditorEventType.IMAGE_SET_OVERVIEW_SELECTED, "#editorContentImageSetOverviewPane");
    }

    protected void updateContent(int index) {
        imageSetOverviewContentPane.getChildren().clear();

        var counter = new AtomicInteger(0);
        projectService.getActiveArtivact().getImageSets()
                .forEach(imageSet -> imageSetOverviewContentPane.getChildren().add(createPreview(imageSet, counter.getAndIncrement())));
    }

    private Pane createPreview(ArtivactImageSet imageSet, int index) {
        if (!imageSet.getImages().isEmpty()) {

            Pane preview = createRawPreview(projectService.getActiveArtivact(), imageSet.getImages().get(0));

            var horizontalBox = new HBox();

            var indexLabel = new Label(String.format("%03d", index));
            HBox.setMargin(indexLabel, new Insets(12, 0, 0, 0));
            horizontalBox.getChildren().add(indexLabel);

            var detailsSpacer = new Pane();
            HBox.setHgrow(detailsSpacer, Priority.ALWAYS);
            horizontalBox.getChildren().add(detailsSpacer);

            var backgroundRemovedIcon = new FontIcon("fas-draw-polygon");

            backgroundRemovedIcon.setIconSize(24);
            HBox.setMargin(backgroundRemovedIcon, new Insets(5, 5, 0, 0));
            if (imageSet.getBackgroundRemoved() == null) {
                backgroundRemovedIcon.setIconColor(Paint.valueOf("lightgrey"));
                addTooltip(backgroundRemovedIcon, "editor.content.image-set-overview.background-status.unknown.tooltip");
            } else if (Boolean.TRUE.equals(imageSet.getBackgroundRemoved())) {
                backgroundRemovedIcon.setIconColor(Paint.valueOf("green"));
                addTooltip(backgroundRemovedIcon, "editor.content.image-set-overview.background-status.removed.tooltip");
            } else {
                backgroundRemovedIcon.setIconColor(Paint.valueOf("red"));
                addTooltip(backgroundRemovedIcon, "editor.content.image-set-overview.background-status.existing.tooltip");
            }
            horizontalBox.getChildren().add(backgroundRemovedIcon);

            var modelInputIcon = new FontIcon("fas-cube");
            modelInputIcon.setIconSize(24);
            HBox.setMargin(modelInputIcon, new Insets(5, 0, 0, 0));
            if (imageSet.isModelInput()) {
                modelInputIcon.setIconColor(Paint.valueOf("green"));
                addTooltip(modelInputIcon, "editor.content.image-set-overview.model-input.true.tooltip");
            } else {
                modelInputIcon.setIconColor(Paint.valueOf("red"));
                addTooltip(modelInputIcon, "editor.content.image-set-overview.model-input.false.tooltip");
            }
            horizontalBox.getChildren().add(modelInputIcon);

            ((VBox) preview.getChildren().get(0)).getChildren().add(horizontalBox);

            return preview;
        } else {
            var stackPane = new StackPane();
            FlowPane.clearConstraints(stackPane);
            FlowPane.setMargin(stackPane, new Insets(5));

            return stackPane;
        }
    }

}
