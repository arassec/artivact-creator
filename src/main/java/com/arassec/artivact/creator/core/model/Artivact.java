package com.arassec.artivact.creator.core.model;

import com.arassec.artivact.creator.core.util.FileHelper;
import com.arassec.artivact.creator.core.util.ProgressMonitor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Data
public class Artivact {

    private static final String DATA_DIR = "Data";

    private static final String IMAGES_DIR = "images";

    private static final String IMAGES_PREVIEW_DIR = "preview";

    private static final String MODELS_DIR = "models";

    private static final String MESHROOM_DIR = "Meshroom";

    private static final String MESHROOM_CACHE_DIR = "MeshroomCache";

    private static final String MESHROOM_RESULT_DIR = "MeshroomResult";

    private static final String BLENDER_DIR = "Blender";

    private static final String MISC = "Misc";

    private static final String CAMERA_PI_DIR = "CameraPi";

    private static final String FALLBACK_IMAGE = "Utils/fallback-image.png";

    private String id;

    private String notes;

    private List<ArtivactImageSet> imageSets = new LinkedList<>();

    private List<ArtivactModel> models = new LinkedList<>();

    @JsonIgnore
    private Path projectRoot;

    @JsonIgnore
    private FileHelper fileHelper;

    public Artivact() {
    }

    public Artivact(String id) {
        this.id = id;
    }

    public String getPreviewImage() {
        return imageSets.stream()
                .filter(imageSet -> !imageSet.getImages().isEmpty())
                .map(imageSet -> imageSet.getImages().get(0).getPreview())
                .findFirst()
                .orElse(FALLBACK_IMAGE);
    }

    public Path getMainDir(boolean includeProjectRoot) {
        var firstSubDir = getSubDir(0, id);
        var secondSubDir = getSubDir(1, id);
        if (includeProjectRoot) {
            return Path.of(projectRoot.toAbsolutePath().toString(), DATA_DIR, firstSubDir, secondSubDir, id);
        }
        return Path.of(DATA_DIR, firstSubDir, secondSubDir, id);
    }

    public Path getImagesDir(boolean includeProjectRoot) {
        return getAssetDir(includeProjectRoot, id, IMAGES_DIR);
    }

    public Path getImagesPreviewDir(boolean includeProjectRoot) {
        Path imagesDir = getImagesDir(includeProjectRoot);
        return Path.of(imagesDir.toString(), IMAGES_PREVIEW_DIR);
    }

    public Path getModelsDir(boolean includeProjectRoot, String artivactId) {
        return getAssetDir(includeProjectRoot, artivactId, MODELS_DIR);
    }

    public Path getModelDir(boolean includeProjectRoot, String artivactId, int assetNumber) {
        return Path.of(getModelsDir(includeProjectRoot, artivactId).toString(), getAssetName(assetNumber, null));
    }

    public ArtivactImage createImage(File asset) {
        log.debug("Creating new image: {}", asset.getPath());
        fileHelper.createDirIfRequired(getImagesPreviewDir(true));

        int nextAssetNumber = getNextAssetNumber(getImagesDir(true));
        String[] assetNameParts = asset.getName().split("\\.");
        var extension = "";
        if (assetNameParts.length > 1) {
            extension = assetNameParts[assetNameParts.length - 1];
        }

        var targetFile = getImagePath(false, nextAssetNumber, extension);
        var targetFileWithProjectRoot = getImagePath(true, nextAssetNumber, extension);

        var previewFile = getImagePreviewPath(false, nextAssetNumber, extension);
        var previewFileWithProjectRoot = getImagePreviewPath(true, nextAssetNumber, extension);

        try {
            Files.copy(Path.of(asset.getPath()), targetFileWithProjectRoot);
            log.debug("Image copied to target dir: {}", targetFileWithProjectRoot);
            writeImagePreview(asset, extension, previewFileWithProjectRoot.toFile());
            log.debug("Image preview created: {}", previewFileWithProjectRoot);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not copy asset!", e);
        }

        return ArtivactImage.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetFile))
                .preview(formatPath(previewFile))
                .build();
    }

    public Path getImagePath(boolean includeProjectRoot, int assetNumber, String extension) {
        var firstSubDir = getSubDir(0, id);
        var secondSubDir = getSubDir(1, id);
        var imageName = getAssetName(assetNumber, extension);
        if (includeProjectRoot) {
            return projectRoot.resolve(Path.of(DATA_DIR, firstSubDir, secondSubDir, id, IMAGES_DIR, imageName));
        }
        return Path.of(DATA_DIR, firstSubDir, secondSubDir, id, IMAGES_DIR, imageName);
    }

    public Path getImagePreviewPath(boolean includeProjectRoot, int assetNumber, String extension) {
        var firstSubDir = getSubDir(0, id);
        var secondSubDir = getSubDir(1, id);
        var imageName = getAssetName(assetNumber, extension);
        if (includeProjectRoot) {
            return projectRoot.resolve(Path.of(DATA_DIR, firstSubDir, secondSubDir, id, IMAGES_DIR,
                    IMAGES_PREVIEW_DIR, imageName));
        }
        return Path.of(DATA_DIR, firstSubDir, secondSubDir, id, IMAGES_DIR, IMAGES_PREVIEW_DIR, imageName);
    }

    public void deleteImage(ArtivactImage asset) {
        imageSets.forEach(imageSet -> {
            if (imageSet.getImages().contains(asset)) {
                imageSet.getImages().remove(asset);
                try {
                    Files.deleteIfExists(asset.getPreviewPath(projectRoot));
                    Files.deleteIfExists(projectRoot.resolve(Path.of(asset.getPath())));
                } catch (IOException e) {
                    throw new ArtivactCreatorException("Could not delete Images!", e);
                }
            }
        });
    }

    public void deleteImageSet(ArtivactImageSet artivactImageSet) {
        if (getImageSets().contains(artivactImageSet)) {
            List<ArtivactImage> images = new LinkedList<>(artivactImageSet.getImages());
            images.forEach(this::deleteImage);
            imageSets.remove(artivactImageSet);
        }
    }

    public void addModel(Path modelFile) {
        Path modelsDir = getModelsDir(true, id);

        fileHelper.createDirIfRequired(modelsDir);

        int nextAssetNumber = getNextAssetNumber(modelsDir);

        var targetDir = getModelDir(false, id, nextAssetNumber);
        var targetDirWithProjectRoot = getModelDir(true, id, nextAssetNumber);

        fileHelper.createDirIfRequired(targetDirWithProjectRoot);

        var destination = Paths.get(targetDirWithProjectRoot.toString(), modelFile.getFileName().toString());
        try {
            Files.copy(modelFile, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fileHelper.deleteDir(targetDirWithProjectRoot);
            throw new ArtivactCreatorException("Could not copy model files!", e);
        }
        ArtivactModel artivactModel = ArtivactModel.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetDir))
                .preview(FALLBACK_IMAGE)
                .comment("import")
                .exportFiles(new LinkedList<>())
                .build();

        getModels().add(artivactModel);
    }

    public void createModel(Path sourceDir, String comment) {
        Path modelsDir = getModelsDir(true, id);

        fileHelper.createDirIfRequired(modelsDir);

        int nextAssetNumber = getNextAssetNumber(modelsDir);

        var targetDir = getModelDir(false, id, nextAssetNumber);
        var targetDirWithProjectRoot = getModelDir(true, id, nextAssetNumber);

        fileHelper.createDirIfRequired(targetDirWithProjectRoot);

        try (Stream<Path> stream = Files.list(sourceDir)) {
            if (stream.findAny().isEmpty()) {
                fileHelper.deleteDir(targetDirWithProjectRoot);
                throw new ArtivactCreatorException("Source for model creation not present: " + sourceDir);
            }

            try (Stream<Path> sourceStream = Files.walk(sourceDir)) {
                sourceStream.forEach(source -> {
                    var destination = Paths.get(targetDirWithProjectRoot.toString(), source.toString()
                            .substring(sourceDir.toString().length()));
                    try {
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        fileHelper.deleteDir(targetDirWithProjectRoot);
                        throw new ArtivactCreatorException("Could not copy model files!", e);
                    }
                });
            }

        } catch (IOException e) {
            fileHelper.deleteDir(targetDirWithProjectRoot);
            throw new ArtivactCreatorException("Could not copy asset directory!", e);
        }

        ArtivactModel artivactModel = ArtivactModel.builder()
                .number(nextAssetNumber)
                .path(formatPath(targetDir))
                .preview(FALLBACK_IMAGE)
                .comment(comment)
                .exportFiles(new LinkedList<>())
                .build();

        getModels().add(artivactModel);
    }

    public void deleteModel(int index) {
        if (models.size() > index) {
            ArtivactModel modelToDelete = getModels().get(index);
            fileHelper.deleteDir(projectRoot.resolve(modelToDelete.getPath()));
            models.remove(index);
        }
    }

    public void openDirInOs(Path directory) {
        var osString = System.getProperty("os.name");
        String commandString;
        if (!Files.exists(directory)) {
            directory = getMainDir(true);
        }
        if (osString.contains("Windows")) {
            commandString = "cmd /c start " + directory.toAbsolutePath();
        } else {
            commandString = "xdg-open " + directory.toAbsolutePath();
        }
        try {
            Runtime.getRuntime().exec(commandString);
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not open directory!", e);
        }
    }

    public void addImageSet(List<File> images, ProgressMonitor progressMonitor, Boolean backgroundRemoved, boolean modelInput) {
        if (images != null && !images.isEmpty()) {
            List<ArtivactImage> artivactImages = new LinkedList<>();
            var index = new AtomicInteger(0);
            images.forEach(image -> {
                var asset = createImage(image);
                artivactImages.add(asset);
                progressMonitor.updateProgress("(" + index.addAndGet(1) + "/" + images.size() + ")");
            });
            imageSets.add(new ArtivactImageSet(modelInput, backgroundRemoved, artivactImages));
        }
    }

    public void deleteArtivactDir(Path rootDir) {
        fileHelper.deleteDir(rootDir.resolve(getMainDir(false)));
        var firstSubDir = rootDir.resolve(DATA_DIR).resolve(getSubDir(0, id));
        var secondSubDir = Path.of(firstSubDir.toString(), getSubDir(1, id));
        try {
            try (Stream<Path> stream = Files.list(secondSubDir)) {
                if (stream.findAny().isEmpty()) {
                    fileHelper.deleteDir(secondSubDir);
                }
            }
            try (Stream<Path> stream = Files.list(firstSubDir)) {
                if (stream.findAny().isEmpty()) {
                    fileHelper.deleteDir(firstSubDir);
                }
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not delete all directories for Artivact-ID: " + id);
        }
    }

    private int getNextAssetNumber(Path dir) {
        var highestNumber = 0;
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> assets = stream.toList();
            for (Path path : assets) {
                if (IMAGES_PREVIEW_DIR.equals(path.getFileName().toString())
                        || ".".equals(path.getFileName().toString())
                        || "..".equals(path.getFileName().toString())) {
                    continue;
                }
                var number = Integer.parseInt(path.getFileName().toString().split("\\.")[0]);
                if (number > highestNumber) {
                    highestNumber = number;
                }
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not read assets!", e);
        }
        return (highestNumber + 1);
    }

    private String formatPath(Path path) {
        return path.toString().replace("\\", "/");
    }

    private Path getAssetDir(boolean includeProjectRoot, String artivactId, String assetSubDir) {
        var firstSubDir = getSubDir(0, artivactId);
        var secondSubDir = getSubDir(1, artivactId);
        if (includeProjectRoot) {
            return projectRoot.resolve(Path.of(DATA_DIR, firstSubDir, secondSubDir, artivactId, assetSubDir));
        }
        return Path.of(DATA_DIR, firstSubDir, secondSubDir, artivactId, assetSubDir);
    }

    private String getSubDir(int index, String artivactId) {
        if (index == 0) {
            return artivactId.substring(0, 3);
        } else if (index == 1) {
            return artivactId.substring(3, 6);
        }
        throw new IllegalArgumentException("Index not supported: " + index);
    }

    private void writeImagePreview(File image, String extension, File targetFile) throws IOException {
        var targetWidth = 100;
        var targetHeight = 100;

        BufferedImage originalImage = ImageIO.read(image);
        var resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        var type = BufferedImage.TYPE_INT_ARGB;
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")) {
            type = BufferedImage.TYPE_INT_RGB;
        }
        var outputImage = new BufferedImage(targetWidth, targetHeight, type);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        ImageIO.write(outputImage, extension, targetFile);
    }

    private String getAssetName(int assetNumber, String extension) {
        if (extension != null && !extension.isEmpty() && !extension.strip().isBlank()) {
            return String.format("%03d", assetNumber) + "." + extension;
        }
        return String.format("%03d", assetNumber);
    }

}
