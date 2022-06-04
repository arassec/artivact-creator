package com.arassec.artivact.creator.core.adapter.image.turntable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FallbackTurntableAdapter implements TurntableAdapter {

    @Override
    public void rotate(int numPhotos) {
        log.info("Fallback turntable called with 'numPhotos': {}", numPhotos);
    }

}
