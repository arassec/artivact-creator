package com.arassec.artivact.creator.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;
import org.kordamp.ikonli.javafx.FontIcon;

public class ArtivactSummary {

    @Getter
    @Setter
    private StackPane previewImage;

    @Getter
    @Setter
    private String artivactId;

    private final StringProperty notes = new SimpleStringProperty();

    @Getter
    @Setter
    private FontIcon imagesAvailable;

    @Getter
    @Setter
    private FontIcon modelsAvailable;

    @Getter
    @Setter
    private FontIcon modelsEdited;

    @Getter
    @Setter
    private FontIcon modelsExported;

    public final StringProperty notesProperty() {
        return this.notes;
    }

    public final java.lang.String getNotes() {
        return this.notesProperty().get();
    }

    public final void setNotes(final java.lang.String notes) {
        this.notesProperty().set(notes);
    }

}
