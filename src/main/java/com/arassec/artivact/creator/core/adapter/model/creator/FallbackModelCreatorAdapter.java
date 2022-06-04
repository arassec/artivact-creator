package com.arassec.artivact.creator.core.adapter.model.creator;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class FallbackModelCreatorAdapter implements ModelCreatorAdapter {

    @Override
    public String getDefaultPipeline() {
        return "fallback-default";
    }

    @Override
    public List<String> getPipelines() {
        return List.of("fallback-default", "fallback-extended");
    }

    @Override
    public void createModel(Artivact artivact, String pipeline, ProgressMonitor progressMonitor) {
        log.info("Fallback model creator called for artivact: {}", artivact.getId());
    }

    @Override
    public void cancelModelCreation() {
        log.info("Fallback model creator called to cancel model creation.");
    }

}
