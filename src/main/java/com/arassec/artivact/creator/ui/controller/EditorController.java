package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.service.ExportService;
import com.arassec.artivact.creator.core.service.ImageService;
import com.arassec.artivact.creator.core.service.ModelService;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import com.arassec.artivact.creator.ui.event.EditorEvent;
import com.arassec.artivact.creator.ui.event.EditorEventType;
import com.arassec.artivact.creator.ui.event.SceneConfig;
import com.arassec.artivact.creator.ui.event.SceneEvent;
import com.arassec.artivact.creator.ui.event.SceneEventType;
import com.arassec.artivact.creator.ui.util.DialogHelper;
import com.arassec.artivact.creator.ui.util.LongRunningOperation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditorController implements ApplicationEventPublisherAware, ApplicationListener<EditorEvent> {

    private static final String I18N_OK = "general.ok";

    private static final String I18N_DONE = "general.done";

    private static final String CONTENT_PANE_STYLE = "-fx-border: none; -fx-focus-color: none;";

    private final ProjectService projectService;

    private final ImageService imageService;

    private final ModelService modelService;

    private final ExportService exportService;

    private final DialogHelper dialogHelper;

    private final MessageSource messageSource;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    private ApplicationEventPublisher applicationEventPublisher;

    private boolean initialized = false;

    @FXML
    private Pane editorTreePane;

    @FXML
    private ScrollPane editorContentImageSetOverviewPane;

    @FXML
    private ScrollPane editorContentImageSetPane;

    @FXML
    private ScrollPane editorContentModelOverviewPane;

    @FXML
    private ScrollPane editorContentModelPane;

    @FXML
    private Button backButton;

    @FXML
    private SplitPane contentSplitPane;

    @FXML
    private Pane spacer;

    @FXML
    public void initialize() {
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox.setVgrow(contentSplitPane, Priority.ALWAYS);

        backButton.setOnAction(event -> {
                    projectService.saveArtivact(projectService.getActiveArtivact());
                    applicationEventPublisher.publishEvent(
                            new SceneEvent(SceneEventType.LOAD_SCENE, new SceneConfig(SceneEvent.PROJECT_HOME_FXML, null)));
                }
        );

        editorContentImageSetOverviewPane.setStyle(CONTENT_PANE_STYLE);
        editorContentImageSetOverviewPane.setVisible(true);

        editorContentImageSetPane.setStyle(CONTENT_PANE_STYLE);
        editorContentImageSetPane.setVisible(false);

        editorContentModelOverviewPane.setStyle(CONTENT_PANE_STYLE);
        editorContentModelOverviewPane.setVisible(false);

        editorContentModelPane.setStyle(CONTENT_PANE_STYLE);
        editorContentModelPane.setVisible(false);

        initialized = true;
    }

    @PreDestroy
    public void teardown() {
        executor.shutdownNow();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void onApplicationEvent(EditorEvent event) {
        if (!initialized) {
            return;
        }
        switch (event.getType()) {
            case IMAGE_SET_OVERVIEW_SELECTED -> {
                editorContentImageSetOverviewPane.setVisible(true);
                editorContentImageSetPane.setVisible(false);
                editorContentModelOverviewPane.setVisible(false);
                editorContentModelPane.setVisible(false);
            }
            case IMAGE_SET_SELECTED -> {
                editorContentImageSetOverviewPane.setVisible(false);
                editorContentImageSetPane.setVisible(true);
                editorContentModelOverviewPane.setVisible(false);
                editorContentModelPane.setVisible(false);
            }
            case MODEL_OVERVIEW_SELECTED -> {
                editorContentImageSetOverviewPane.setVisible(false);
                editorContentImageSetPane.setVisible(false);
                editorContentModelOverviewPane.setVisible(true);
                editorContentModelPane.setVisible(false);
            }
            case MODEL_SELECTED -> {
                editorContentImageSetOverviewPane.setVisible(false);
                editorContentImageSetPane.setVisible(false);
                editorContentModelOverviewPane.setVisible(false);
                editorContentModelPane.setVisible(true);
            }
            case TOGGLE_MODEL_INPUT -> toggleModelInput(event.getIndex());
            case REMOVE_BACKGROUND -> removeBackgroundsFromImageSet(event.getIndex());
            case EDIT_MODEL -> editModel(event.getIndex());
            case DELETE_IMAGE_SET -> deleteImageSet(event.getIndex());
            case DELETE_MODEL -> deleteModel(event.getIndex());
            default -> log.trace("Event received in EditorController: {}", event);
        }
    }

    public void addImages() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(messageSource.getMessage("editor.dialog.add-images.title", null, Locale.getDefault()));
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("jpg", "jpeg", "png"));
        List<File> images = fileChooser.showOpenMultipleDialog(editorTreePane.getScene().getWindow());

        var progressMonitor = new ProgressMonitor();
        progressMonitor.setProgressPrefix(messageSource.getMessage("editor.dialog.add-images.progress.prefix", null,
                Locale.getDefault()));

        executor.execute(new LongRunningOperation(editorTreePane.getScene().getWindow(), progressMonitor,
                () -> {
                    projectService.getActiveArtivact().addImageSet(images, progressMonitor, null, true);
                    return messageSource.getMessage(I18N_OK, null, Locale.getDefault());
                },
                () -> {
                    projectService.saveArtivact(projectService.getActiveArtivact());
                    applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE_AND_SELECT_IMAGE_SET_OVERVIEW, -1));
                    return messageSource.getMessage(I18N_DONE, null, Locale.getDefault());
                },
                () -> cancelProgress(progressMonitor),
                applicationEventPublisher, projectService.getActiveArtivact()
        ));
    }

    public void captureImage() {
        var progressMonitor = new ProgressMonitor();
        executor.execute(new LongRunningOperation(editorTreePane.getScene().getWindow(), progressMonitor,
                () -> {
                    imageService.capturePhotos(projectService.getActiveArtivact(), 1, false, progressMonitor);
                    return messageSource.getMessage(I18N_OK, null, Locale.getDefault());
                },
                () -> {
                    projectService.saveArtivact(projectService.getActiveArtivact());
                    applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE_AND_SELECT_IMAGE_SET_OVERVIEW, -1));
                    return messageSource.getMessage(I18N_DONE, null, Locale.getDefault());
                },
                () -> cancelProgress(progressMonitor),
                applicationEventPublisher, null
        ));
    }

    public void captureImages() {
        Optional<Pair<Integer, Boolean>> userInputOptional = dialogHelper.showScanImagesDialog(editorTreePane.getScene().getWindow());
        if (userInputOptional.isEmpty()) {
            return;
        }

        Pair<Integer, Boolean> userInput = userInputOptional.get();

        projectService.saveArtivact(projectService.getActiveArtivact());

        var progressMonitor = new ProgressMonitor();
        executor.execute(new LongRunningOperation(editorTreePane.getScene().getWindow(), progressMonitor,
                () -> {
                    imageService.capturePhotos(projectService.getActiveArtivact(), userInput.getKey(), userInput.getValue(),
                            progressMonitor);
                    return messageSource.getMessage(I18N_OK, null, Locale.getDefault());
                },
                () -> {
                    projectService.saveArtivact(projectService.getActiveArtivact());
                    applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE_AND_SELECT_IMAGE_SET_OVERVIEW, -1));
                    return messageSource.getMessage(I18N_DONE, null, Locale.getDefault());
                },
                () -> cancelProgress(progressMonitor),
                applicationEventPublisher, projectService.getActiveArtivact()
        ));
    }

    public void removeBackgroundsFromImageSet(int imageSetIndex) {
        var progressMonitor = new ProgressMonitor();
        executor.execute(new LongRunningOperation(editorTreePane.getScene().getWindow(), progressMonitor,
                () -> {
                    var artivact = projectService.getActiveArtivact();
                    List<Path> images = imageService.removeBackgrounds(artivact, artivact.getImageSets().get(imageSetIndex),
                            progressMonitor);
                    artivact.addImageSet(images.stream().map(Path::toFile).collect(Collectors.toList()), progressMonitor, true,
                            true);
                    artivact.getImageSets().forEach(imageSet -> {
                        if (!Boolean.TRUE.equals(imageSet.getBackgroundRemoved())) {
                            imageSet.setModelInput(false);
                        }
                    });
                    return messageSource.getMessage(I18N_OK, null, Locale.getDefault());
                },
                () -> {
                    projectService.saveArtivact(projectService.getActiveArtivact());
                    applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE_AND_SELECT_IMAGE_SET_OVERVIEW, -1));
                    return messageSource.getMessage(I18N_DONE, null, Locale.getDefault());
                },
                null, // Cannot be cancelled!
                applicationEventPublisher, projectService.getActiveArtivact()
        ));
    }

    private void toggleModelInput(Integer imageSetIndex) {
        ArtivactImageSet selectedImageSet = projectService.getActiveArtivact().getImageSets().get(imageSetIndex);
        selectedImageSet.setModelInput(!selectedImageSet.isModelInput());
        projectService.saveArtivact(projectService.getActiveArtivact());
        applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE, -1));
    }

    public void addModel() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(messageSource.getMessage("editor.dialog.add-model.title", null, Locale.getDefault()));
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("glb", "glb"));
        var file = fileChooser.showOpenDialog(editorTreePane.getScene().getWindow());

        projectService.getActiveArtivact().addModel(file.toPath());
        projectService.saveArtivact(projectService.getActiveArtivact());
        applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE, -1));
    }

    public void createModel() {
        ChoiceDialog<String> pipelineDialog = new ChoiceDialog<>(modelService.getDefaultPipeline(), modelService.getPipelines());
        pipelineDialog.initModality(Modality.APPLICATION_MODAL);
        pipelineDialog.setGraphic(null);
        pipelineDialog.initOwner(editorTreePane.getScene().getWindow());
        pipelineDialog.setTitle(messageSource.getMessage("editor.dialog.create-model.title", null, Locale.getDefault()));
        pipelineDialog.setHeaderText(messageSource.getMessage("editor.dialog.create-model.header", null, Locale.getDefault()));
        pipelineDialog.setContentText(messageSource.getMessage("editor.dialog.create-model.content", null, Locale.getDefault()));

        String pipeline = pipelineDialog.showAndWait().orElse(null);
        if (pipeline == null) {
            return;
        }

        projectService.saveArtivact(projectService.getActiveArtivact());

        var progressMonitor = new ProgressMonitor();
        executor.execute(new LongRunningOperation(editorTreePane.getScene().getWindow(), progressMonitor,
                () -> {
                    modelService.createModel(projectService.getActiveArtivact(), pipeline, progressMonitor);
                    return messageSource.getMessage(I18N_OK, null, Locale.getDefault());
                },
                () -> {
                    projectService.saveArtivact(projectService.getActiveArtivact());
                    applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE, -1));
                    return messageSource.getMessage(I18N_DONE, null, Locale.getDefault());
                },
                () -> {
                    modelService.cancelModelCreation();
                    return cancelProgress(progressMonitor);
                },
                applicationEventPublisher, projectService.getActiveArtivact()
        ));
    }

    private void editModel(int modelIndex) {
        var artivact = projectService.getActiveArtivact();

        var progressMonitor = new ProgressMonitor();
        progressMonitor.setProgressPrefix(messageSource.getMessage("editor.dialog.edit-model.progress.prefix", null, Locale.getDefault()));

        executor.execute(new LongRunningOperation(editorTreePane.getScene().getWindow(), progressMonitor,
                () -> {
                    modelService.openModel(artivact, artivact.getModels().get(modelIndex));
                    return messageSource.getMessage(I18N_OK, null, Locale.getDefault());
                },
                () -> {
                    projectService.saveArtivact(projectService.getActiveArtivact());
                    applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_MODEL_CONTENT, modelIndex));
                    return messageSource.getMessage(I18N_DONE, null, Locale.getDefault());
                },
                null, applicationEventPublisher, null
        ));
    }

    private void deleteImageSet(Integer imageSetIndex) {
        if (imageSetIndex < 0) {
            return;
        }
        Optional<ButtonType> result = dialogHelper.showDeleteConfirmDialog(editorTreePane.getScene().getWindow());
        if (result.orElse(ButtonType.NO) == ButtonType.OK) {
            projectService.getActiveArtivact().deleteImageSet(projectService.getActiveArtivact().getImageSets().get(imageSetIndex));
            projectService.saveArtivact(projectService.getActiveArtivact());
            applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE_AND_SELECT_IMAGE_SET_OVERVIEW, -1));
        }
    }

    private void deleteModel(int modelIndex) {
        if (modelIndex < 0) {
            return;
        }
        Optional<ButtonType> result = dialogHelper.showDeleteConfirmDialog(editorTreePane.getScene().getWindow());
        if (result.orElse(ButtonType.NO) == ButtonType.OK) {
            projectService.getActiveArtivact().deleteModel(modelIndex);
            projectService.saveArtivact(projectService.getActiveArtivact());
            applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_EDITOR_TREE_AND_SELECT_IMAGE_SET_OVERVIEW, -1));
        }
    }

    private String cancelProgress(ProgressMonitor progressMonitor) {
        progressMonitor.setProgressPrefix(messageSource.getMessage("general.cancelling", null, Locale.getDefault()));
        progressMonitor.setCancelled(true);
        return messageSource.getMessage("general.cancelled", null, Locale.getDefault());
    }

    public void export() {
        var progressMonitor = new ProgressMonitor("Exporting - ");
        executor.execute(new LongRunningOperation(editorTreePane.getScene().getWindow(), progressMonitor,
                () -> {
                    exportService.export(projectService.getActiveArtivact(), progressMonitor);
                    return messageSource.getMessage(I18N_OK, null, Locale.getDefault());
                },
                () -> messageSource.getMessage(I18N_DONE, null, Locale.getDefault()),
                null, applicationEventPublisher, null
        ));
    }

}
