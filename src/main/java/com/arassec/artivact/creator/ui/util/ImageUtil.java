package com.arassec.artivact.creator.ui.util;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

public final class ImageUtil {

    private static Background homePreviewBackground;

    private static Background editorPreviewBackground;

    private ImageUtil() {
    }

    public static Background getHomePreviewBackground(Path projectRoot) {
        if (homePreviewBackground == null) {
            homePreviewBackground = getBackground(projectRoot, 50);
        }
        return homePreviewBackground;
    }

    public static Background getEditorPreviewBackground(Path projectRoot) {
        if (editorPreviewBackground == null) {
            editorPreviewBackground = getBackground(projectRoot, 100);
        }
        return editorPreviewBackground;
    }

    private static Background getBackground(Path projectRoot, int size) {
        try (var fileInputStream = new FileInputStream(projectRoot.resolve("Utils/checkerboard.png").toFile())) {
            var backgroundimage = new BackgroundImage(new Image(fileInputStream),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.DEFAULT,
                    new BackgroundSize(size, size, true, true, true, false));

            return new Background(backgroundimage);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create checkerboard background image!", e);
        }
    }

}
