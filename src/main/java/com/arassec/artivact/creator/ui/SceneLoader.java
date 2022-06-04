package com.arassec.artivact.creator.ui;

import com.arassec.artivact.creator.ui.event.SceneConfig;
import com.arassec.artivact.creator.ui.event.SceneEvent;
import com.arassec.artivact.creator.ui.event.SceneEventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class SceneLoader implements ApplicationListener<SceneEvent> {

    private final String applicationTitle;

    private final ApplicationContext applicationContext;

    private final Image applicationLogo;

    private final ResourceBundle labelsResourceBundle;

    private Scene scene;

    private Stage stage;

    private Region opaqueLayer;

    public SceneLoader(@Value("${com.arassec.artivact.creator.title}") String applicationTitle,
                       @Value("classpath:misc/application-logo.png") Resource logoResource,
                       ApplicationContext applicationContext,
                       MessageSource messageSource) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = applicationContext;
        this.labelsResourceBundle = new MessageSourceResourceBundle(messageSource, Locale.getDefault());
        try {
            this.applicationLogo = new Image(logoResource.getURL().openStream());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load application logo!", e);
        }
    }

    @Override
    public void onApplicationEvent(SceneEvent event) {
        if (SceneEventType.LOAD_SCENE.equals(event.getType())) {
            loadScene(event.getConfig());
        } else if (SceneEventType.MODAL_OPENED.equals(event.getType())) {
            opaqueLayer.resizeRelocate(0, 0, scene.getWidth(), scene.getHeight());
            opaqueLayer.setVisible(true);
        } else if (SceneEventType.MODAL_CLOSED.equals(event.getType())) {
            opaqueLayer.setVisible(false);
        }
    }

    private void loadScene(SceneConfig sceneConfig) {

        boolean initialization = (stage == null);
        if (initialization) {
            stage = sceneConfig.getStage();
            stage.setTitle(applicationTitle);
            stage.getIcons().add(applicationLogo);
        }

        try {
            if (scene == null || !StringUtils.hasText(sceneConfig.getFxml())) {
                scene = new Scene(createProjectChooserRoot(), 640, 480);

                stage.setScene(scene);
                stage.setWidth(640);
                stage.setHeight(480);
                stage.setResizable(false);
            } else {
                opaqueLayer = new Region();
                opaqueLayer.setStyle("-fx-background-color: #00000055;");
                opaqueLayer.setVisible(false);

                scene.setRoot(new StackPane(createSceneRoot(sceneConfig.getFxml()), opaqueLayer));
                stage.setWidth(1024);
                stage.setHeight(768);
                stage.setResizable(true);
            }

            scene.getRoot().requestFocus();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load FXML scene!", e);
        }

        if (initialization) {
            stage.show();
        }
    }

    private VBox createProjectChooserRoot() throws IOException {
        var url = new ClassPathResource(SceneEvent.PROJECT_CHOOSER_FXML).getURL();

        var fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        fxmlLoader.setResources(labelsResourceBundle);

        Parent sceneRoot = fxmlLoader.load();

        return new VBox(sceneRoot);
    }

    private VBox createSceneRoot(String fxml) throws IOException {
        var url = new ClassPathResource(fxml).getURL();

        var fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        fxmlLoader.setResources(labelsResourceBundle);

        Parent sceneRoot = fxmlLoader.load();

        VBox.setVgrow(sceneRoot, Priority.ALWAYS);
        return new VBox(sceneRoot);
    }

}
