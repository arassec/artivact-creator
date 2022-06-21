package com.arassec.artivact.creator.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtivactImage extends ArtivactAsset {

    @Override
    public AssetType getType() {
        return AssetType.IMAGE;
    }

    private boolean export;

}
