package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.AssetType;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.ui.event.EditorEvent;
import com.arassec.artivact.creator.ui.event.EditorEventType;
import com.arassec.artivact.creator.ui.model.EditorTree;
import com.arassec.artivact.creator.ui.model.EditorTreeItemContent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class EditorTreeController implements ApplicationEventPublisherAware, ApplicationListener<EditorEvent> {

    private final ProjectService projectService;

    private final MessageSource messageSource;

    private ApplicationEventPublisher applicationEventPublisher;

    @FXML
    private AnchorPane editorTreeContainer;

    @FXML
    private EditorTree editorTree;

    @FXML
    public void initialize() {
        editorTree.setContainer(editorTreeContainer);

        editorTree.createContextMenu(messageSource);

        editorTree.getSelectionModel().selectedItemProperty().addListener((observableValue, editorTreeItemContentTreeItem, selectedTreeItem) -> {
            if (selectedTreeItem == null || selectedTreeItem.getValue() == null) {
                return;
            }
            var assetType = selectedTreeItem.getValue().getAssetType();
            int index = selectedTreeItem.getValue().getIndex();

            if (AssetType.IMAGE.equals(assetType) && index < 0) {
                applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.IMAGE_SET_OVERVIEW_SELECTED, -1));
            } else if (AssetType.IMAGE.equals(assetType)) {
                applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.IMAGE_SET_SELECTED, index));
            } else if (AssetType.MODEL.equals(assetType) && index < 0) {
                applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.MODEL_OVERVIEW_SELECTED, -1));
            } else if (AssetType.MODEL.equals(assetType)) {
                applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.MODEL_SELECTED, index));
            }
        });

        configureContextMenu();

        updateTreeNavigation(false);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void onApplicationEvent(EditorEvent event) {
        if (EditorEventType.UPDATE_EDITOR_TREE.equals(event.getType())) {
            updateTreeNavigation(false);
        } else if (EditorEventType.UPDATE_EDITOR_TREE_AND_SELECT_IMAGE_SET_OVERVIEW.equals(event.getType())) {
            updateTreeNavigation(true);
        }
    }

    private void configureContextMenu() {
        editorTree.getRemoveBackgroundMenuItem().setOnAction(actionEvent -> applicationEventPublisher.publishEvent(
                new EditorEvent(EditorEventType.REMOVE_BACKGROUND,
                        editorTree.getSelectionModel().getSelectedItem().getValue().getIndex())
        ));

        editorTree.getToggleModelInputMenutItem().setOnAction(actionEvent -> {
            TreeItem<EditorTreeItemContent> selectedItem = editorTree.getSelectionModel().getSelectedItem();
            applicationEventPublisher.publishEvent(
                    new EditorEvent(EditorEventType.TOGGLE_MODEL_INPUT, selectedItem.getValue().getIndex())
            );
        });

        editorTree.getEditModelMenuItem().setOnAction(actionEvent -> applicationEventPublisher.publishEvent(
                new EditorEvent(EditorEventType.EDIT_MODEL,
                        editorTree.getSelectionModel().getSelectedItem().getValue().getIndex()))
        );

        editorTree.getOpenFsMenuItem().setOnAction(actionEvent -> {
            TreeItem<EditorTreeItemContent> selectedTreeItem = editorTree.getSelectionModel().getSelectedItem();
            if (selectedTreeItem == null || selectedTreeItem.getValue() == null) {
                return;
            }
            var assetType = selectedTreeItem.getValue().getAssetType();
            int index = selectedTreeItem.getValue().getIndex();

            if (AssetType.IMAGE.equals(assetType)) {
                projectService.getActiveArtivact().openDirInOs(projectService.getActiveArtivact().getImagesDir(true));
            } else if (AssetType.MODEL.equals(assetType) && index < 0) {
                projectService.getActiveArtivact().openDirInOs(projectService.getActiveArtivact().getModelsDir(true,
                        projectService.getActiveArtivact().getId()));
            } else if (AssetType.MODEL.equals(assetType)) {
                var artivactModel = projectService.getActiveArtivact().getModels().get(index);
                projectService.getActiveArtivact().openDirInOs(projectService.getActiveArtivact().getModelDir(true,
                        projectService.getActiveArtivact().getId(), artivactModel.getNumber()));
            }
        });

        editorTree.getDeleteMenuItem().setOnAction(actionEvent -> {
                    TreeItem<EditorTreeItemContent> selectedItem = editorTree.getSelectionModel().getSelectedItem();
                    if (selectedItem == null || selectedItem.getValue() == null) {
                        return;
                    }
                    var assetType = selectedItem.getValue().getAssetType();
                    int index = selectedItem.getValue().getIndex();
                    if (AssetType.IMAGE.equals(assetType) && index >= 0) {
                        applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.DELETE_IMAGE_SET, index));
                    } else if (AssetType.MODEL.equals(assetType) && index >= 0) {
                        applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.DELETE_MODEL, index));
                    }
                }
        );
    }

    private void updateTreeNavigation(boolean selectImageSetOverview) {
        TreeItem<EditorTreeItemContent> imageSetsTreeItem = new TreeItem<>(
                new EditorTreeItemContent(AssetType.IMAGE, -1,
                        messageSource.getMessage("editor.tree.image-sets", null, Locale.getDefault()), null));
        imageSetsTreeItem.setExpanded(true);
        imageSetsTreeItem.setGraphic(new FontIcon("fas-images"));

        var artivact = projectService.getActiveArtivact();

        for (var i = 0; i < artivact.getImageSets().size(); i++) {
            TreeItem<EditorTreeItemContent> imageItem =
                    new TreeItem<>(new EditorTreeItemContent(AssetType.IMAGE, i, "", null));

            var hBox = new HBox(5);
            var label = new Label(String.format("%03d", i + 1));
            label.setGraphic(new FontIcon("fas-image"));
            hBox.getChildren().add(label);

            if (Boolean.TRUE.equals(artivact.getImageSets().get(i).getBackgroundRemoved())) {
                var sp = new StackPane();
                sp.getChildren().add(new FontIcon("fas-draw-polygon"));
                hBox.getChildren().add(sp);
            }

            if (artivact.getImageSets().get(i).isModelInput()) {
                var sp = new StackPane();
                sp.getChildren().add(new FontIcon("fas-cube"));
                hBox.getChildren().add(sp);
            }

            imageItem.setGraphic(hBox);
            imageSetsTreeItem.getChildren().add(imageItem);
        }

        TreeItem<EditorTreeItemContent> models = new TreeItem<>(new EditorTreeItemContent(AssetType.MODEL, -1,
                messageSource.getMessage("editor.tree.models", null, Locale.getDefault()), null));
        models.setExpanded(true);
        models.setGraphic(new FontIcon("fas-cubes"));
        for (var i = 0; i < artivact.getModels().size(); i++) {
            TreeItem<EditorTreeItemContent> modelItem =
                    new TreeItem<>(new EditorTreeItemContent(AssetType.MODEL, i, "", artivact.getModels().get(i)));
            var label = new Label(String.format("%03d", i + 1));
            label.setGraphic(new FontIcon("fas-cube"));
            modelItem.setGraphic(label);

            models.getChildren().add(modelItem);
        }

        TreeItem<EditorTreeItemContent> root = new TreeItem<>();
        root.setExpanded(true);
        root.getChildren().add(imageSetsTreeItem);
        root.getChildren().add(models);

        editorTree.setRoot(root);

        if (selectImageSetOverview) {
            editorTree.getSelectionModel().select(imageSetsTreeItem);
        }
    }

}
