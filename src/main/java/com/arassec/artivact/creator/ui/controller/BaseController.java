package com.arassec.artivact.creator.ui.controller;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public abstract class BaseController {

    protected ImageView createPreviewImageView(InputStream imageInputStream) {
        var image = new Image(imageInputStream);

        var imageView = new ImageView();
        imageView.setImage(image);
        imageView.setX(10);
        imageView.setY(10);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        return imageView;
    }

}
