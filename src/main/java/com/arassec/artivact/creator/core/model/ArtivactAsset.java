package com.arassec.artivact.creator.core.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.nio.file.Path;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public abstract class ArtivactAsset {

    protected int number;

    protected String path;

    protected String preview;

    public abstract AssetType getType();

    public Path getPreviewPath(Path projectRoot) {
        return projectRoot.resolve(preview);
    }

}
