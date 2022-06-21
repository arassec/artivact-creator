package com.arassec.artivact.creator.core.adapter.export;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.ArtivactImage;
import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import com.arassec.artivact.creator.core.model.ArtivactModel;
import com.arassec.artivact.creator.core.util.FileHelper;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArtivactVaultExportAdapter implements ExportAdapter {

    private final FileHelper fileHelper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getId() {
        return "adapter.implementation.export.artivact-vault";
    }

    @Override
    public void export(Artivact artivact, Path targetDir, ProgressMonitor progressMonitor) {
        Path exportDir = targetDir.resolve(artivact.getMainDir(false));

        fileHelper.deleteDir(exportDir);
        fileHelper.createDirIfRequired(exportDir);

        var imagesExported = exportImages(artivact, exportDir, artivact.getImageSets().stream()
                .map(ArtivactImageSet::getImages)
                .flatMap(Collection::stream)
                .filter(ArtivactImage::isExport)
                .collect(Collectors.toList()));

        var modelsExported = exportModels(artivact, exportDir, artivact.getModels());

        var dataExported = exportData(artivact, exportDir);

        if (!imagesExported && !modelsExported && !dataExported) {
            artivact.deleteArtivactDir(targetDir);
        }
    }

    private boolean exportImages(Artivact artivact, Path exportDir, List<ArtivactImage> images) {
        if (images.isEmpty()) {
            return false;
        }

        Path imageExportDir = exportDir.resolve("images");
        fileHelper.createDirIfRequired(imageExportDir);

        for (var i = 0; i < images.size(); i++) {
            String targetFilename = String.format("%03d", i) + "." + determineFileEnding(images.get(i).getPath());
            try {
                Files.copy(artivact.getProjectRoot().resolve(images.get(i).getPath()), imageExportDir.resolve(targetFilename),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not export image!", e);
            }
        }

        return true;
    }

    private boolean exportModels(Artivact artivact, Path exportDir, List<ArtivactModel> models) {
        var modelsExported = false;

        Path modelExportDir = exportDir.resolve("models");
        fileHelper.createDirIfRequired(modelExportDir);

        for (var i = 0; i < models.size(); i++) {
            for (var exportFile : models.get(i).getExportFiles()) {
                modelsExported = true;
                String targetFilename = String.format("%03d", i) + "." + determineFileEnding(exportFile);
                try {
                    Files.copy(artivact.getProjectRoot().resolve(artivact.getModelDir(true, artivact.getId(),
                                    models.get(i).getNumber())).resolve(exportFile), modelExportDir.resolve(targetFilename),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new ArtivactCreatorException("Could not export image!", e);
                }
            }
        }

        if (!modelsExported) {
            fileHelper.deleteDir(modelExportDir);
        }

        return modelsExported;
    }

    private boolean exportData(Artivact artivact, Path targetDir) {
        if (StringUtils.hasText(artivact.getNotes())) {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("notes", artivact.getNotes());
                Files.writeString(targetDir.resolve("data.json").toAbsolutePath(), objectMapper.writeValueAsString(data));
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not create notes file during export!", e);
            }
            return true;
        }
        return false;
    }

    private String determineFileEnding(String fileName) {
        String[] parts = fileName.split("\\.");
        return parts[parts.length - 1];
    }
}
