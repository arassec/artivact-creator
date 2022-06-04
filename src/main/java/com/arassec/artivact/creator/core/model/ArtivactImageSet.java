package com.arassec.artivact.creator.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArtivactImageSet {

    private boolean modelInput;

    private Boolean backgroundRemoved;

    @Builder.Default
    private List<ArtivactImage> images = new LinkedList<>();

}
