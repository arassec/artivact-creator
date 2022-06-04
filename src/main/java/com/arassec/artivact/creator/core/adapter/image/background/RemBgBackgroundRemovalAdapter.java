package com.arassec.artivact.creator.core.adapter.image.background;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.util.FileHelper;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "adapter.implementation.background", havingValue = "RemBg")
public class RemBgBackgroundRemovalAdapter implements BackgroundRemovalAdapter {

    private final FileHelper fileHelper;

    private final MessageSource messageSource;

    @Value("${adapter.implementation.background.executable}")
    private String executable;

    public List<Path> removeBackgroundFromImages(Artivact artivact, ArtivactImageSet imageSet,
                                                 ProgressMonitor progressMonitor) {
        Path tempDir = artivact.getProjectRoot().resolve(FileHelper.TEMP_DIR);

        Path remBgInputDir = tempDir.resolve("rembg-input");
        Path remBgOutputDir = tempDir.resolve("rembg-output");

        fileHelper.emptyDir(tempDir);
        fileHelper.createDirIfRequired(remBgInputDir);
        fileHelper.createDirIfRequired(remBgOutputDir);

        fileHelper.copyImages(artivact, imageSet, remBgInputDir, progressMonitor);

        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);

        progressMonitor.setProgressPrefix(messageSource.getMessage("background-removal-adapter.rembg.progress.prefix", null,
                Locale.getDefault()));

        log.debug("Removing background of images in: {}", tempDir.toAbsolutePath());
        var processBuilder = new ProcessBuilder(executable, "-p",
                remBgInputDir.toAbsolutePath().toString(), remBgOutputDir.toAbsolutePath().toString());
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        try {
            var process = processBuilder.start();
            process.waitFor();

            try (var inputStream = Files.list(remBgOutputDir)) {
                return inputStream.collect(Collectors.toList());
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not remove backgrounds from images!", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArtivactCreatorException("Interrupted during background removal!", e);
        }
    }

}
