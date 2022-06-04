package com.arassec.artivact.creator.core.service;

import com.arassec.artivact.creator.core.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.creator.core.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.creator.core.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.util.FileHelper;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final TurntableAdapter turntableAdapter;

    private final CameraAdapter cameraAdapter;

    private final BackgroundRemovalAdapter backgroundRemovalAdapter;

    private final FileHelper fileHelper;

    private final MessageSource messageSource;

    public void capturePhotos(Artivact artivact, int numPhotos, boolean useTurnTable, ProgressMonitor progressMonitor) {
        Path targetDir = artivact.getProjectRoot().resolve(FileHelper.TEMP_DIR);

        fileHelper.emptyDir(targetDir);

        progressMonitor.setProgressPrefix(messageSource.getMessage("image-service.capture-photos.progress.prefix", null, Locale.getDefault()));

        for (var i = 0; i < numPhotos; i++) {

            if (progressMonitor.isCancelled()) {
                return;
            }

            progressMonitor.setProgress("(" + (i + 1) + "/" + numPhotos + ")");
            cameraAdapter.captureImage(targetDir);
            if (useTurnTable) {
                turntableAdapter.rotate(numPhotos);
            }
        }

        try (Stream<Path> imagePaths = Files.list(targetDir)) {
            List<File> images = imagePaths
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            progressMonitor.setProgressPrefix(messageSource.getMessage("editor.dialog.add-images.progress.prefix", null, Locale.getDefault()));

            artivact.addImageSet(images, progressMonitor, false, useTurnTable);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not add captured images!", e);
        }
    }

    public List<Path> removeBackgrounds(Artivact artivact, ArtivactImageSet imageSet,
                                                 ProgressMonitor progressMonitor) {
        return backgroundRemovalAdapter.removeBackgroundFromImages(artivact, imageSet, progressMonitor);
    }

}
