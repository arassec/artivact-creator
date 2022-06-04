package com.arassec.artivact.creator.core.service;

import com.arassec.artivact.creator.core.adapter.model.creator.ModelCreatorAdapter;
import com.arassec.artivact.creator.core.adapter.model.editor.ModelEditorAdapter;
import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactAsset;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelCreatorAdapter modelCreatorAdapter;

    private final ModelEditorAdapter modelEditorAdapter;

    public String getDefaultPipeline() {
        return modelCreatorAdapter.getDefaultPipeline();
    }

    public List<String> getPipelines() {
        return modelCreatorAdapter.getPipelines();
    }

    public void createModel(Artivact artivact, String pipeline, ProgressMonitor progressMonitor) {
        modelCreatorAdapter.createModel(artivact, pipeline, progressMonitor);
    }

    public void cancelModelCreation() {
        modelCreatorAdapter.cancelModelCreation();
    }

    public void openModel(Artivact artivact, ArtivactAsset asset) {
        modelEditorAdapter.openModel(artivact, asset);
    }

}
