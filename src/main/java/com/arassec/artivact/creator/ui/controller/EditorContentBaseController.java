package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactAsset;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.ui.event.EditorEvent;
import com.arassec.artivact.creator.ui.event.EditorEventType;
import com.arassec.artivact.creator.ui.util.ImageUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public abstract class EditorContentBaseController extends BaseController implements ApplicationEventPublisherAware,
        ApplicationListener<EditorEvent> {

    protected static final String PREVIEW_PANE_CSS = "-fx-padding: 5; -fx-border-color: black; -fx-border-width: 1;";

    protected final ProjectService projectService;

    protected final MessageSource messageSource;

    protected ApplicationEventPublisher applicationEventPublisher;

    private FlowPane contentPane;

    private EditorEventType relevantEventType;

    private String parentId;

    protected EditorContentBaseController(ProjectService projectService, MessageSource messageSource) {
        this.projectService = projectService;
        this.messageSource = messageSource;
    }

    protected void initialize(FlowPane contentPane, EditorEventType relevantEventType, String parentId) {
        this.contentPane = contentPane;
        this.relevantEventType = relevantEventType;
        this.parentId = parentId;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void onApplicationEvent(EditorEvent event) {
        if (relevantEventType != null && relevantEventType.equals(event.getType())) {
            contentPane.setStyle("-fx-border: none; -fx-focus-color: none;");
            contentPane.prefWidthProperty().bind(
                    ((ScrollPane) contentPane.getScene().lookup(parentId)).widthProperty()
            );
            updateContent(event.getIndex());
        }
    }

    protected abstract void updateContent(int index);

    protected Pane createRawPreview(Artivact artivact, ArtivactAsset asset) {
        return createRawPreview(asset.getPreviewPath(artivact.getProjectRoot()), true, true);
    }

    protected Pane createRawPreview(Path filePath, boolean border, boolean background) {
        try (var fileInputStream = new FileInputStream(filePath.toString())) {

            var imageView = createPreviewImageView(fileInputStream);

            var imagePane = new StackPane();
            imagePane.getChildren().add(imageView);
            if (background) {
                imagePane.setBackground(ImageUtil.getEditorPreviewBackground(projectService.getActiveArtivact().getProjectRoot()));
            }
            if (border) {
                imagePane.setStyle("-fx-border-style: 1px solid; -fx-border-color: black");
            }

            var vbox = new VBox();
            vbox.getChildren().add(imagePane);

            return createPreviewPane(vbox);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not create model preview!", e);
        }
    }

    protected void addTooltip(FontIcon icon, String i18nKey) {
        var tooltip = new Tooltip(messageSource.getMessage(i18nKey, null, Locale.getDefault()));
        tooltip.setFont(new Font(12));
        Tooltip.install(icon, tooltip);
    }

    private Pane createPreviewPane(VBox vbox) {
        var stackPane = new StackPane();
        stackPane.getChildren().add(vbox);
        stackPane.setStyle(PREVIEW_PANE_CSS);
        stackPane.setOnMouseEntered(mouseEvent -> stackPane.setCursor(Cursor.HAND));

        FlowPane.clearConstraints(stackPane);
        FlowPane.setMargin(stackPane, new Insets(5));

        return stackPane;
    }

}
