package com.arassec.artivact.creator.ui.controller;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.service.ExportService;
import com.arassec.artivact.creator.core.service.ModelService;
import com.arassec.artivact.creator.core.service.ProjectService;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import com.arassec.artivact.creator.ui.event.SceneConfig;
import com.arassec.artivact.creator.ui.event.SceneEvent;
import com.arassec.artivact.creator.ui.event.SceneEventType;
import com.arassec.artivact.creator.ui.model.ArtivactSummary;
import com.arassec.artivact.creator.ui.util.DialogHelper;
import com.arassec.artivact.creator.ui.util.ImageUtil;
import com.arassec.artivact.creator.ui.util.LongRunningOperation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectHomeController implements ApplicationEventPublisherAware {

    private final ProjectService projectService;

    private final ModelService modelService;

    private final ExportService exportService;

    private final MessageSource messageSource;

    private final DialogHelper dialogHelper;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    @Getter
    private final ObservableList<ArtivactSummary> artivactSummaries = FXCollections.observableArrayList();

    private ApplicationEventPublisher applicationEventPublisher;

    @FXML
    private ScrollPane artivactsTableContainer;

    @FXML
    private TableView<ArtivactSummary> artivactsTable;

    @FXML
    private Pane spacer;

    @FXML
    public void initialize() {
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox.setVgrow(artivactsTableContainer, Priority.ALWAYS);
        VBox.setVgrow(artivactsTable, Priority.ALWAYS);

        artivactsTable.setStyle("-fx-focus-color: transparent;"
                + "-fx-faint-focus-color: transparent;"
                + "-fx-selection-bar: lightgrey;"
                + "-fx-selection-bar-non-focused: none;"
                 + "-fx-border: none;"
        );

        artivactsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox.setVgrow(artivactsTable, Priority.ALWAYS);

        reloadSummaryPage();
    }

    @PreDestroy
    public void teardown() {
        executor.shutdownNow();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void closeProject() {
        applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.LOAD_SCENE, new SceneConfig()));
    }

    public void addArtivact() {
        projectService.initializeActiveArtivact(projectService.createArtivact());
        applicationEventPublisher.publishEvent(new SceneEvent(
                SceneEventType.LOAD_SCENE, new SceneConfig(SceneEvent.ARTIVACT_EDITOR_FXML, null)));
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    public void createModels() {
        List<Artivact> artivactsWithoutModels = projectService.getArtivactIds().stream()
                .map(projectService::readArtivact)
                .filter(artivact -> artivact.getModels().isEmpty())
                .collect(Collectors.toUnmodifiableList());

        if (artivactsWithoutModels.isEmpty()) {
            return;
        }

        var progressMonitor = new ProgressMonitor();
        for (Artivact artivact : artivactsWithoutModels) {
            executor.execute(new LongRunningOperation(artivactsTable.getScene().getWindow(), progressMonitor,
                    () -> {
                        modelService.createModel(artivact, modelService.getDefaultPipeline(), progressMonitor);
                        return messageSource.getMessage("general.ok", null, Locale.getDefault());
                    },
                    () -> {
                        projectService.saveArtivact(artivact);
                        return messageSource.getMessage("general.done", null, Locale.getDefault());
                    },
                    () -> {
                        modelService.cancelModelCreation();
                        progressMonitor.setProgress(messageSource.getMessage("general.cancelling", null, Locale.getDefault()));
                        progressMonitor.setCancelled(true);
                        executor.shutdownNow();
                        return messageSource.getMessage("general.cancelled", null, Locale.getDefault());
                    },
                    applicationEventPublisher, null
            ));
        }
    }

    private void reloadSummaryPage() {
        artivactSummaries.clear();

        var editItem = new MenuItem(messageSource.getMessage("general.edit", null, Locale.getDefault()));
        editItem.setOnAction(event -> {
            ArtivactSummary selectedItem = artivactsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                projectService.initializeActiveArtivact(selectedItem.getArtivactId());
                applicationEventPublisher.publishEvent(new SceneEvent(
                        SceneEventType.LOAD_SCENE, new SceneConfig(SceneEvent.ARTIVACT_EDITOR_FXML, null)));
            }
        });

        var deleteItem = new MenuItem(messageSource.getMessage("general.delete", null, Locale.getDefault()));
        deleteItem.setOnAction(event -> {
            ArtivactSummary selectedItem = artivactsTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Optional<ButtonType> result = dialogHelper.showDeleteConfirmDialog(artivactsTable.getScene().getWindow());
                if (result.orElse(ButtonType.NO) == ButtonType.OK) {
                    projectService.deleteArtivact(projectService.readArtivact(selectedItem.getArtivactId()));
                    artivactsTable.getItems().remove(selectedItem);
                }
            }
        });

        var contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-selection-bar: lightgrey;");
        contextMenu.getItems().add(editItem);
        contextMenu.getItems().add(new SeparatorMenuItem());
        contextMenu.getItems().add(deleteItem);

        //noinspection SimplifyStreamApiCallChains
        List<ArtivactSummary> items = projectService.getArtivactIds()
                .stream()
                .map(artivactId -> {
                    var artivact = projectService.readArtivact(artivactId);

                    var imageView = new ImageView();
                    try (var fileInputStream = new FileInputStream(
                            projectService.getActiveProject().getAssetPath(artivact.getPreviewImage()).toFile())) {

                        var image = new Image(fileInputStream);

                        imageView.setImage(image);
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);
                        imageView.setPreserveRatio(false);

                    } catch (IOException e) {
                        throw new ArtivactCreatorException("Could not read image!", e);
                    }

                    var imagePane = new StackPane();
                    imagePane.setPrefWidth(50);
                    imagePane.getChildren().add(imageView);
                    imagePane.setBackground(ImageUtil.getHomePreviewBackground(projectService.getActiveProject().getRootDir()));
                    imagePane.setStyle("-fx-border-style: 1px solid; -fx-border-color: lightgrey");

                    var artivactSummary = new ArtivactSummary();
                    artivactSummary.setPreviewImage(imagePane);
                    artivactSummary.setArtivactId(artivactId);
                    artivactSummary.setNotes(artivact.getNotes());

                    final var check = new AtomicBoolean(false);
                    artivact.getImageSets().forEach(imageSet -> {
                        if (!imageSet.getImages().isEmpty()) {
                            check.set(true);
                        }
                    });
                    artivactSummary.setImagesAvailable(createIcon(check.get()));

                    artivactSummary.setModelsAvailable(createIcon(!artivact.getModels().isEmpty()));

                    artivactSummary.setModelsEdited(createIcon(checkModelFiles(artivact, ".blend").orElse(null)));

                    artivactSummary.setModelsExported(createIcon(checkModelFiles(artivact, ".glb").orElse(null)));

                    return artivactSummary;
                })
                .collect(Collectors.toUnmodifiableList());

        artivactSummaries.addAll(items);

        artivactsTable.setContextMenu(contextMenu);
    }

    private FontIcon createIcon(Boolean available) {
        FontIcon icon;
        if (Boolean.TRUE.equals(available)) {
            icon = new FontIcon("far-check-circle");
            icon.setIconColor(Paint.valueOf("green"));
        } else if (Boolean.FALSE.equals(available)) {
            icon = new FontIcon("far-times-circle");
            icon.setIconColor(Paint.valueOf("red"));
        } else {
            icon = new FontIcon("far-circle");
        }
        icon.setIconSize(32);
        return icon;
    }

    private Optional<Boolean> checkModelFiles(Artivact artivact, String ending) {
        final var result = new AtomicBoolean(false);
        artivact.getModels().forEach(model -> {
            try (var fileStream = Files.list(artivact.getProjectRoot().resolve(model.getPath()))) {
                fileStream.forEach(file -> {
                    if (file.getFileName().toString().endsWith(ending)) {
                        result.set(true);
                    }
                });
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not read model dir!", e);
            }
        });

        if (result.get()) {
            return Optional.of(Boolean.TRUE);
        }
        return Optional.empty();
    }

    public void export() {
        var progressMonitor = new ProgressMonitor("Exporting - ");
        executor.execute(new LongRunningOperation(artivactsTable.getScene().getWindow(), progressMonitor,
                () -> {
                    projectService.getArtivactIds().forEach(artivactId -> {
                        progressMonitor.setProgressPrefix("Exporting " + artivactId + " - ");
                        exportService.export(projectService.readArtivact(artivactId), progressMonitor);
                    });
                    return messageSource.getMessage("general.ok", null, Locale.getDefault());
                },
                () -> messageSource.getMessage("general.done", null, Locale.getDefault()),
                null, applicationEventPublisher, null
        ));
    }
}
