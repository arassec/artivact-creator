package com.arassec.artivact.creator.core.adapter.image.background;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

@Slf4j
public class FallbackBackgroundRemovalAdapter implements BackgroundRemovalAdapter {

    @Override
    public List<Path> removeBackgroundFromImages(Artivact artivact, ArtivactImageSet imageSet, ProgressMonitor progressMonitor) {
        log.info("Fallback background removal adapter called for artivact: {}", artivact.getId());
        return List.of();
    }

}
