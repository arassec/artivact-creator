package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.Project;
import com.arassec.artivact.creator.core.service.ConfigurationService;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.core.util.FileHelper;
import com.arassec.artivact.creator.ui.event.SceneConfig;
import com.arassec.artivact.creator.ui.event.SceneEvent;
import com.arassec.artivact.creator.ui.event.SceneEventType;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ProjectChooserController implements ApplicationEventPublisherAware {

    @FXML
    public Button newProjectButton;

    @FXML
    public Button openProjectButton;

    @FXML
    private Pane spacer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox recentProjectsList;

    private ApplicationEventPublisher publisher;

    private final MessageSource messageSource;

    private final FileHelper fileHelper;

    private final ProjectService projectService;

    private final ConfigurationService configurationService;

    @FXML
    public void initialize() {
        HBox.setHgrow(spacer, Priority.ALWAYS);

        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color:transparent;");
        scrollPane.setPrefHeight(600);

        recentProjectsList.setPadding(new Insets(5));

        loadRecentProjects();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

    public void openProject() {
        var projectDir = chooseDirectory("project-chooser.open-project.dialog-title");

        if (projectDir == null) {
            return;
        }

        if (!projectService.isProjectDir(projectDir.toPath())) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(messageSource.getMessage("project-chooser.open-project.error.dialog-title", null, Locale.getDefault()));
            alert.setContentText(messageSource.getMessage("project-chooser.open-project.error.dialog-text", null, Locale.getDefault()));
            alert.setHeaderText(null);
            alert.initOwner(spacer.getScene().getWindow());
            alert.showAndWait();
        } else {
            projectService.updateProject(Path.of(projectDir.getAbsolutePath()));
            configurationService.addRecentProject(projectDir.getAbsolutePath());
            projectService.setActiveProject(new Project(Path.of(projectDir.getAbsolutePath())));
            publisher.publishEvent(new SceneEvent(
                    SceneEventType.LOAD_SCENE, new SceneConfig(SceneEvent.PROJECT_HOME_FXML, null)));
        }
    }

    public void newProject() {
        var projectDir = chooseDirectory("project-chooser.new-project.dialog-title");

        if (projectDir == null) {
            return;
        }

        if (!fileHelper.isDirEmpty(projectDir.toPath())) {
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(messageSource.getMessage("project-chooser.new-project.error.dialog-title", null, Locale.getDefault()));
            alert.setContentText(messageSource.getMessage("project-chooser.new-project.error.dialog-text", null,
                    Locale.getDefault()));
            alert.setHeaderText(null);
            alert.initOwner(spacer.getScene().getWindow());
            alert.showAndWait();
        } else {
            projectService.initializeProjectDir(projectDir.toPath());
            configurationService.addRecentProject(projectDir.getAbsolutePath());
            projectService.setActiveProject(new Project(Path.of(projectDir.getAbsolutePath())));
            publisher.publishEvent(new SceneEvent(SceneEventType.LOAD_SCENE, new SceneConfig(SceneEvent.PROJECT_HOME_FXML, null)));
        }
    }

    private File chooseDirectory(String titleKey) {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new JFileChooser().getFileSystemView().getDefaultDirectory());
        directoryChooser.setTitle(messageSource.getMessage(titleKey, null, Locale.getDefault()));

        return directoryChooser.showDialog(spacer.getScene().getWindow());
    }

    private void loadRecentProjects() {
        recentProjectsList.getChildren().clear();
        List<String> recentProjects = configurationService.getRecentProjects();
        recentProjects.forEach(this::createRecentProjectPane);
    }

    private void createRecentProjectPane(String recentProject) {
        Pane pane = new GridPane();
        pane.setPadding(new Insets(10));
        pane.setBackground(new Background(new BackgroundFill(Color.GAINSBORO,new CornerRadii(10), Insets.EMPTY)));
        pane.setStyle("-fx-cursor: hand;");
        pane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(25));

        pane.setOnMouseClicked(event -> {
            projectService.updateProject(Path.of(recentProject));
            projectService.setActiveProject(new Project(Path.of(recentProject)));
            publisher.publishEvent(new SceneEvent(SceneEventType.LOAD_SCENE, new SceneConfig(SceneEvent.PROJECT_HOME_FXML, null)));
        });

        var path = Path.of(recentProject);

        var projectTitle = new Text(path.getFileName().toString());
        projectTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        var projectBox = new VBox();
        projectBox.getChildren().add(projectTitle);
        projectBox.getChildren().add(new Label(recentProject));

        var spacerPane = new Pane();
        HBox.setHgrow(spacerPane, Priority.ALWAYS);

        var closeButton = new Button();
        var fontIcon = new FontIcon("fas-times");
        fontIcon.setIconSize(16);
        closeButton.setGraphic(fontIcon);
        closeButton.setStyle("-fx-cursor: pointer;");
        closeButton.setTooltip(new Tooltip(messageSource.getMessage("project-chooser.remove-from-recent-projects", null,
                Locale.getDefault())));
        closeButton.setOnAction(actionEvent -> {
            configurationService.removeRecentProject(recentProject);
            loadRecentProjects();
        });

        var buttonBox = new VBox();
        buttonBox.getChildren().add(closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        var hBox = new HBox();
        hBox.prefWidthProperty().bind(pane.widthProperty());
        hBox.getChildren().add(projectBox);
        hBox.getChildren().add(spacerPane);
        hBox.getChildren().add(buttonBox);

        pane.getChildren().add(hBox);

        recentProjectsList.getChildren().add(pane);
    }
}
