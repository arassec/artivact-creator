package com.arassec.artivact.creator.core.adapter.model.editor;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactAsset;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.AssetType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
@Component
@ConditionalOnProperty(value = "adapter.implementation.model-editor", havingValue = "Blender")
public class BlenderModelEditorAdapter implements ModelEditorAdapter {

    private static final String BLENDER_DIR = "Utils/Blender";

    @Value("${adapter.implementation.model-editor.executable}")
    private String executable;

    public void openModel(Artivact artivact, ArtivactAsset asset) {
        if (!AssetType.MODEL.equals(asset.getType())) {
            throw new ArtivactCreatorException("Only models can be opened in Blender!");
        }

        var modelPath =artivact.getProjectRoot().resolve(asset.getPath());

        var blenderProjectExists = new AtomicBoolean(false);
        var blenderProjectFile = new StringBuilder();
        checkBlenderFile(modelPath, blenderProjectExists, blenderProjectFile);

        var cmdLine = new CommandLine(executable);

        if (blenderProjectExists.get()) {
            cmdLine.addArgument(blenderProjectFile.toString());
        } else {
            cmdLine.addArgument("--python");
            cmdLine.addArgument(artivact.getProjectRoot().resolve(BLENDER_DIR).resolve("blender-obj-import.py")
                    .toAbsolutePath().toString());
            cmdLine.addArgument("--");
            cmdLine.addArgument(modelPath.toAbsolutePath().toString());
        }

        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        try {
            executor.execute(cmdLine, resultHandler);

            // some time later the result handler callback was invoked so we
            // can safely request the exit value
            resultHandler.waitFor();

            if (resultHandler.getExitValue() != 0 && resultHandler.getException() != null) {
                log.error("Could not open Blender!", resultHandler.getException());
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not open model in Blender!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactCreatorException("Interrupted during Blender session!", e);
        }
    }

    private void checkBlenderFile(Path modelPath, AtomicBoolean blenderProjectExistsTarget,
                                  StringBuilder blenderProjectFileTarget) {
        try (Stream<Path> stream = Files.list(modelPath)) {
            stream.forEach(path -> {
                if (path.toString().endsWith(".blend")) {
                    blenderProjectExistsTarget.set(true);
                    blenderProjectFileTarget.append(path.toAbsolutePath());
                }
            });
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not determine blender project status!", e);
        }
    }

}
