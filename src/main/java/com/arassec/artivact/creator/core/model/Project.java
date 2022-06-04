package com.arassec.artivact.creator.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    private static final String DATA_DIR = "Data";

    private Path rootDir;

    public Path getDataDir() {
        return rootDir.resolve(DATA_DIR);
    }

    public Path getAssetPath(String assetLocation) {
        return rootDir.resolve(assetLocation);
    }

}
