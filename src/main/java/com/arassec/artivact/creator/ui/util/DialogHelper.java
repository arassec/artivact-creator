package com.arassec.artivact.creator.ui.util;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DialogHelper {

    private final MessageSource messageSource;

    public Optional<Pair<Integer, Boolean>> showScanImagesDialog(Window owner) {
        Dialog<Pair<Integer, Boolean>> dialog = new Dialog<>();
        dialog.setTitle(messageSource.getMessage("editor.dialog.scan-images.title", null, Locale.getDefault()));
        dialog.setHeaderText(messageSource.getMessage("editor.dialog.scan-images.header", null, Locale.getDefault()));
        dialog.initOwner(owner);

        var okButtonType = new ButtonType(messageSource.getMessage("general.ok", null, Locale.getDefault()), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        var grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        var numPhotosInput = new TextField();
        numPhotosInput.setText("36");

        var useTurntableCheckbox = new CheckBox();
        useTurntableCheckbox.setSelected(true);

        grid.add(new Label(messageSource.getMessage("editor.dialog.scan-images.param.amount", null, Locale.getDefault())), 0, 0);
        grid.add(numPhotosInput, 1, 0);
        grid.add(new Label(messageSource.getMessage("editor.dialog.scan-images.param.turntable", null, Locale.getDefault())), 0, 1);
        grid.add(useTurntableCheckbox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return new Pair<>(Integer.valueOf(numPhotosInput.getText()), useTurntableCheckbox.isSelected());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<ButtonType> showDeleteConfirmDialog(Window owner) {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(messageSource.getMessage("editor.dialog.delete-asset.title", null, Locale.getDefault()));
        alert.setHeaderText(messageSource.getMessage("editor.dialog.delete-asset.header", null, Locale.getDefault()));
        alert.setContentText(messageSource.getMessage("editor.dialog.delete-asset.content", null, Locale.getDefault()));
        alert.initOwner(owner);
        return alert.showAndWait();
    }
}
