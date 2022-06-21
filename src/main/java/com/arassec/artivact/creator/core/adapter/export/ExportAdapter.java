package com.arassec.artivact.creator.core.adapter.export;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.util.ProgressMonitor;

import java.nio.file.Path;

public interface ExportAdapter {

    String getId();

    void export(Artivact artivact, Path targetDir, ProgressMonitor progressMonitor);

}
