package com.arassec.artivact.creator.core.util;

import com.arassec.artivact.creator.core.model.Artivact;
import com.arassec.artivact.creator.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.core.model.ArtivactImageSet;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Component
public class FileHelper {

    public static final String TEMP_DIR = "Temp";

    public boolean isDirEmpty(final Path directory) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not check directory!", e);
        }
    }

    public void emptyDir(Path directory) {
        deleteDir(directory);
        createDirIfRequired(directory);
    }

    public void createDirIfRequired(Path directory) {
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not create directory: " + directory, e);
            }
        }
    }

    public void copyClasspathResource(Path classpathResource, Path targetDir) {
        createDirIfRequired(targetDir);

        String sourceDir = classpathResource.toString().replace('\\', '/');

        var classLoader = getClass().getClassLoader();
        try {
            var resourceUrl = classLoader.getResource(sourceDir);
            if (resourceUrl == null) {
                throw new ArtivactCreatorException("Could not open resource: " + sourceDir);
            }
            var resourceDir = resourceUrl.toURI();

            if (resourceDir.toString().contains("!")) {
                copyClasspathResourcesFromJar(resourceDir, targetDir);
            } else {
                copyClasspathResourceFromFilesystem(Path.of(Objects.requireNonNull(resourceDir)), targetDir);
            }

        } catch (URISyntaxException | IOException e) {
            throw new ArtivactCreatorException("Could not create initial project layout!", e);
        }
    }

    public void deleteDir(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not delete directory!", e);
        }
    }

    public void copyImages(Artivact artivact, ArtivactImageSet imageSet, Path destination, ProgressMonitor progressMonitor) {
        var index = new AtomicInteger(1);
        imageSet.getImages().forEach(artivactImage -> {
            progressMonitor.setProgress("Copying image " + index.getAndIncrement() + "/" + imageSet.getImages().size());
            Path source = artivact.getProjectRoot().resolve(artivactImage.getPath());
            String[] parts = artivactImage.getPath().split("/");
            Path target = destination.resolve(parts[parts.length - 1]);
            try {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ArtivactCreatorException("Could not copy artivact images for model creation!", e);
            }
        });
    }

    private void copyClasspathResourcesFromJar(URI resourceDir, Path targetDir) throws IOException {
        String[] parts = resourceDir.toString().split("!");
        try (var fs = FileSystems.newFileSystem(URI.create(parts[0]), new HashMap<>())) {
            var resourcesPath = fs.getPath(parts[1], parts[2]);
            copyClasspathResourceFromFilesystem(resourcesPath, targetDir);
        }
    }

    private void copyClasspathResourceFromFilesystem(Path resourcesPath, Path targetDir) throws IOException {
        Files.walkFileTree(resourcesPath, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                var subDir = resourcesPath.relativize(dir);
                if (StringUtils.hasText(subDir.toString())) {
                    var tarDir = targetDir.resolve(subDir.toString());
                    Files.createDirectories(tarDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                var subFile = resourcesPath.relativize(file);
                if (StringUtils.hasText(subFile.toString())) {
                    var tarFile = targetDir.resolve(subFile.toString());
                    Files.copy(file, tarFile, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // FallbackCameraAdapter copies the fallback image:
                    Files.copy(file, targetDir.resolve("fallback-image.png"), StandardCopyOption.REPLACE_EXISTING);
                }
                return FileVisitResult.CONTINUE;
            }

        });
    }

}
