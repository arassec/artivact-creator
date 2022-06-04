package com.arassec.artivact.creator.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private static final String APP_PROPERTIES_FILE = "application.properties";

    private static final String KEY_RECENT_PROJECTS = "recent-projects";

    private static final String FALLBACK = "fallback";

    private Properties appProperties;

    @PostConstruct
    public void initialize() {
        var propFilePath = Path.of(APP_PROPERTIES_FILE);

        appProperties = new Properties() {
            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {
                return Collections.synchronizedSet(
                        super.entrySet()
                                .stream()
                                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                                .collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        };

        if (!Files.exists(propFilePath)) {
            appProperties.setProperty("adapter.implementation.background", FALLBACK);
            appProperties.setProperty("adapter.implementation.background.executable", "");
            appProperties.setProperty("adapter.implementation.camera", FALLBACK);
            appProperties.setProperty("adapter.implementation.camera.executable", "");
            appProperties.setProperty("adapter.implementation.turntable", FALLBACK);
            appProperties.setProperty("adapter.implementation.model-creator", FALLBACK);
            appProperties.setProperty("adapter.implementation.model-creator.executable", "");
            appProperties.setProperty("adapter.implementation.model-editor", FALLBACK);
            appProperties.setProperty("adapter.implementation.model-editor.executable", "");

            appProperties.setProperty(KEY_RECENT_PROJECTS, "");

            writePropertiesFile();
        } else {
            try (InputStream input = new FileInputStream(propFilePath.toAbsolutePath().toString())) {
                // load a properties file
                appProperties.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read properties file!", e);
            }
        }
    }

    public List<String> getRecentProjects() {
        return new LinkedList<>(Arrays.asList(appProperties.getProperty(KEY_RECENT_PROJECTS).split(",")))
                .stream()
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    public void saveRecentProjects(List<String> recentProjects) {
        appProperties.setProperty(KEY_RECENT_PROJECTS, String.join(",", recentProjects));
        writePropertiesFile();
    }

    public void addRecentProject(String project) {
        List<String> recentProjects = getRecentProjects();
        if (!recentProjects.contains(project)) {
            recentProjects.add(project);
            saveRecentProjects(recentProjects);
        }
    }

    public void removeRecentProject(String project) {
        List<String> recentProjects = getRecentProjects();
        recentProjects.remove(project);
        saveRecentProjects(recentProjects);
    }

    private void writePropertiesFile() {
        var propFilePath = Path.of(APP_PROPERTIES_FILE);
        try (OutputStream output = new FileOutputStream(propFilePath.toAbsolutePath().toString())) {
            appProperties.store(output, null);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write initial properties file!", e);
        }
    }

}
