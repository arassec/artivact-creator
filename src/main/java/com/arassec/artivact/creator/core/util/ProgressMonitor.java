package com.arassec.artivact.creator.core.util;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@Data
@RequiredArgsConstructor
public class ProgressMonitor {

    private String progressPrefix;

    private String progress = "Starting";

    private boolean cancelled;

    public void updateProgress(String progress) {
        if (StringUtils.hasText(progressPrefix)) {
            this.progress = progressPrefix + " " + progress;
        } else {
            this.progress = progress;
        }
    }

    public void setProgressPrefix(String progressPrefix) {
        this.progressPrefix = progressPrefix;
        this.progress = progressPrefix;
    }

}
