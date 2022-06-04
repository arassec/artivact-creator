package com.arassec.artivact.creator.core.adapter.image.background;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.util.ProgressMonitor;

import java.nio.file.Path;
import java.util.List;

public interface BackgroundRemovalAdapter {

    List<Path> removeBackgroundFromImages(Artivact artivact, ArtivactImageSet imageSet,
                                          ProgressMonitor progressMonitor);

}
