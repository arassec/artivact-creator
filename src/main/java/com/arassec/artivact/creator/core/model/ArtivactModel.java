package com.arassec.artivact.creator.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtivactModel extends ArtivactAsset {

    private String comment;

    private List<String> exportFiles = new LinkedList<>();

    @Override
    public AssetType getType() {
        return AssetType.MODEL;
    }

}
