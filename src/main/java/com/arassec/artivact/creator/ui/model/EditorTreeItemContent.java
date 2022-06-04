package com.arassec.artivact.creator.ui.model;

import com.arassec.artivact.creator.core.model.ArtivactAsset;
import com.arassec.artivact.creator.core.model.AssetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditorTreeItemContent {

    private AssetType assetType;

    private int index;

    private String label;

    private ArtivactAsset asset;

    @Override
    public String toString() {
        return label;
    }
}
