package com.arassec.artivact.creator.core.adapter.image.camera;

import com.arassec.artivact.creator.core.util.FileHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class FallbackCameraAdapter implements CameraAdapter {

    private final FileHelper fileHelper;

    @Override
    public void captureImage(Path targetDir) {
        log.info("Fallback camera adapter called with 'targetDir': {}", targetDir);
        fileHelper.copyClasspathResource(Path.of("project-setup/Utils/fallback-image.png"),
                targetDir);
    }

}
