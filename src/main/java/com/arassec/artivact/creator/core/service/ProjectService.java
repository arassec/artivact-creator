package com.arassec.artivact.creator.core.service;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.Project;
import com.arassec.artivact.creator.core.util.FileHelper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private static final String ARTIVACT_FILE_SUFFIX = ".artivact.json";

    private final FileHelper fileHelper;

    private final ObjectMapper objectMapper;

    public ProjectService(FileHelper fileHelper) {
        this.fileHelper = fileHelper;
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Getter
    @Setter
    private Project activeProject;

    @Getter
    private Artivact activeArtivact;

    public boolean isProjectDir(Path projectRoot) {
        return Files.exists(projectRoot.resolve("Utils/checkerboard.png"));
    }

    public void initializeProjectDir(Path projectRoot) {
        updateProject(projectRoot);
    }

    public void updateProject(Path projectRoot) {
        fileHelper.createDirIfRequired(projectRoot.resolve("Data"));
        fileHelper.createDirIfRequired(projectRoot.resolve("Temp"));
        fileHelper.copyClasspathResource(Path.of("project-setup"), projectRoot);
    }

    public void initializeActiveArtivact(String artivactId) {
        activeArtivact = readArtivact(artivactId);
    }

    public void initializeActiveArtivact(Artivact artivact) {
        activeArtivact = artivact;
    }

    public Artivact createArtivact() {
        var artivact = new Artivact(UUID.randomUUID().toString());
        artivact.setProjectRoot(activeProject.getRootDir());
        artivact.setFileHelper(fileHelper);
        saveArtivact(artivact);
        return artivact;
    }

    public Artivact readArtivact(String artivactId) {
        var dummyArtivact = new Artivact(artivactId);
        dummyArtivact.setProjectRoot(activeProject.getRootDir());

        var artivactFile = Path.of(dummyArtivact.getMainDir(true).toString(),
                artivactId + ARTIVACT_FILE_SUFFIX);

        if (!Files.exists(artivactFile)) {
            throw new ArtivactCreatorException("Unkonwn artivact with ID: " + artivactFile);
        }

        try {
            var artivactJson = Files.readString(artivactFile);
            var artivact = objectMapper.readValue(artivactJson, Artivact.class);
            if (artivact.getImageSets() == null) {
                artivact.setImageSets(new LinkedList<>());
            }
            if (artivact.getModels() == null) {
                artivact.setModels(new LinkedList<>());
            }

            artivact.setProjectRoot(activeProject.getRootDir());
            artivact.setFileHelper(fileHelper);

            return artivact;
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not read artivact data set!", e);
        }
    }

    public void saveArtivact(Artivact artivact) {
        if (artivact == null || !StringUtils.hasText(artivact.getId())) {
            throw new ArtivactCreatorException("No ID on artivact to persist!");
        }

        String artivactId = artivact.getId();

        var artivactDir = artivact.getMainDir(true);
        var artivactFile = Path.of(artivactDir.toString(), artivactId + ARTIVACT_FILE_SUFFIX);

        fileHelper.createDirIfRequired(artivactDir);

        try {
            Files.writeString(artivactFile, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(artivact));
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not persist artivact data set!", e);
        }
    }

    public void deleteArtivact(Artivact artivact) {
        artivact.deleteArtivactDir(activeProject.getRootDir());
    }

    public List<String> getArtivactIds() {
        List<String> result = new LinkedList<>();
        findArtivactIdsRecursively(activeProject.getDataDir(), result);
        return result;
    }

    private void findArtivactIdsRecursively(Path root, List<String> target) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            directoryStream.forEach(path -> {
                if (path.getFileName().toString().endsWith(ARTIVACT_FILE_SUFFIX)) {
                    target.add(path.getFileName().toString().replace(ARTIVACT_FILE_SUFFIX, ""));
                } else if (Files.isDirectory(path)) {
                    findArtivactIdsRecursively(path, target);
                }
            });
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not read artivacts!", e);
        }
    }

}
