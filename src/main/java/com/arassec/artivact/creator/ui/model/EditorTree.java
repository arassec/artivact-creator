package com.arassec.artivact.creator.ui.model;

import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.AssetType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.MessageSource;

import java.util.Locale;

@SuppressWarnings("java:S110")
public class EditorTree extends TreeView<EditorTreeItemContent> {

    @Getter
    private MenuItem removeBackgroundMenuItem;

    @Getter
    private MenuItem toggleModelInputMenutItem;

    @Getter
    private MenuItem editModelMenuItem;

    @Getter
    private MenuItem openFsMenuItem;

    @Getter
    private MenuItem deleteMenuItem;

    public EditorTree() {
        setRoot(null);
        setShowRoot(false);
    }

    public void setContainer(AnchorPane container) {
        prefWidthProperty().bind(container.widthProperty());
        prefHeightProperty().bind(container.prefHeightProperty());
        setStyle("-fx-focus-color: transparent;"
                + "-fx-faint-focus-color: transparent;"
                + "-fx-selection-bar: lightgrey;"
                + "-fx-selection-bar-non-focused: none;"
        );
    }

    public void createContextMenu(MessageSource messageSource) {
        if (messageSource == null) {
            throw new ArtivactCreatorException("MessageSource required for editor-tree context menu!");
        }

        var contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-selection-bar: lightgrey;");

        removeBackgroundMenuItem = new MenuItem(messageSource.getMessage("editor.tree.context-menu.remove-background", null,
                Locale.getDefault()), new FontIcon("fas-draw-polygon"));
        toggleModelInputMenutItem = new MenuItem(messageSource.getMessage("editor.tree.context-menu.toggle-model-input", null,
                Locale.getDefault()), new FontIcon("fas-cube"));

        editModelMenuItem = new MenuItem(messageSource.getMessage("editor.tree.context-menu.edit-model", null,
                Locale.getDefault()), new FontIcon("fas-pen"));

        openFsMenuItem = new MenuItem(messageSource.getMessage("editor.tree.context-menu.open-filesystem", null,
                Locale.getDefault()), new FontIcon("fas-folder-open"));
        deleteMenuItem = new MenuItem(messageSource.getMessage("editor.tree.context-menu.delete", null, Locale.getDefault()),
                new FontIcon("fas-trash"));

        contextMenu.getItems().add(removeBackgroundMenuItem);
        contextMenu.getItems().add(toggleModelInputMenutItem);
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(editModelMenuItem);
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(openFsMenuItem);
        contextMenu.getItems().add(deleteMenuItem);

        contextMenu.setOnShowing(windowEvent -> {
            TreeItem<EditorTreeItemContent> selectedTreeItem = getSelectionModel().getSelectedItem();
            if (selectedTreeItem == null || selectedTreeItem.getValue() == null) {
                return;
            }
            var assetType = selectedTreeItem.getValue().getAssetType();
            int index = selectedTreeItem.getValue().getIndex();

            if ((AssetType.IMAGE.equals(assetType) || AssetType.MODEL.equals(assetType)) && index < 0) {
                removeBackgroundMenuItem.setDisable(true);
                toggleModelInputMenutItem.setDisable(true);
                editModelMenuItem.setDisable(true);
                deleteMenuItem.setDisable(true);
            } else if (AssetType.IMAGE.equals(assetType)) {
                removeBackgroundMenuItem.setDisable(false);
                toggleModelInputMenutItem.setDisable(false);
                editModelMenuItem.setDisable(true);
                deleteMenuItem.setDisable(false);
            } else if (AssetType.MODEL.equals(assetType)) {
                removeBackgroundMenuItem.setDisable(true);
                toggleModelInputMenutItem.setDisable(true);
                editModelMenuItem.setDisable(false);
                deleteMenuItem.setDisable(false);
            }
        });

        setContextMenu(contextMenu);
    }

}
