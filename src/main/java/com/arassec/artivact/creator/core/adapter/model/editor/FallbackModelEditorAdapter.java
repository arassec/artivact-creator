package com.arassec.artivact.creator.core.adapter.model.editor;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactAsset;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FallbackModelEditorAdapter implements ModelEditorAdapter {

    @Override
    public void openModel(Artivact artivact, ArtivactAsset asset) {
        log.info("Fallback model editor called for artivact: {}", artivact.getId());
    }

}
