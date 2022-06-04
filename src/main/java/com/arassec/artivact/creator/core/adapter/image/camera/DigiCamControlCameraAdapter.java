package com.arassec.artivact.creator.core.adapter.image.camera;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Service
@ConditionalOnProperty(value = "adapter.implementation.camera", havingValue = "DigiCamControl")
public class DigiCamControlCameraAdapter implements CameraAdapter {

    @Value("${adapter.implementation.camera.executable}")
    private String executable;

    public void captureImage(Path targetDir) {

        var cmdLine = new CommandLine(executable);
        cmdLine.addArgument("/folder");
        cmdLine.addArgument(targetDir.toAbsolutePath().toString());
        cmdLine.addArgument("/capture");

        var resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DaemonExecutor();
        executor.setExitValue(1);

        try {
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            log.error("Exception during camera operation!", e);
        }

        // some time later the result handler callback was invoked so we
        // can safely request the exit value
        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted during camera operation!", e);
            Thread.currentThread().interrupt();
        }
    }

}
