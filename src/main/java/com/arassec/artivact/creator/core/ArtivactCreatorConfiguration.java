package com.arassec.artivact.creator.core;

import com.arassec.artivact.creator.core.adapter.image.background.BackgroundRemovalAdapter;
import com.arassec.artivact.creator.core.adapter.image.background.FallbackBackgroundRemovalAdapter;
import com.arassec.artivact.creator.core.adapter.image.camera.CameraAdapter;
import com.arassec.artivact.creator.core.adapter.image.camera.FallbackCameraAdapter;
import com.arassec.artivact.creator.core.adapter.image.turntable.FallbackTurntableAdapter;
import com.arassec.artivact.creator.core.adapter.image.turntable.TurntableAdapter;
import com.arassec.artivact.creator.core.adapter.model.creator.FallbackModelCreatorAdapter;
import com.arassec.artivact.creator.core.adapter.model.creator.ModelCreatorAdapter;
import com.arassec.artivact.creator.core.adapter.model.editor.FallbackModelEditorAdapter;
import com.arassec.artivact.creator.core.adapter.model.editor.ModelEditorAdapter;
import com.arassec.artivact.creator.core.util.FileHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class ArtivactCreatorConfiguration {

    @Bean
    public MessageSource messageSource() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/artivact-creator-labels");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public TurntableAdapter fallbackTurntableAdapter() {
        return new FallbackTurntableAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public CameraAdapter fallbackCameraAdapter(FileHelper fileHelper) {
        return new FallbackCameraAdapter(fileHelper);
    }

    @Bean
    @ConditionalOnMissingBean
    public BackgroundRemovalAdapter fallbackBackgroundRemovalAdapter() {
        return new FallbackBackgroundRemovalAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelCreatorAdapter fallbackModelCreatorAdapter() {
        return new FallbackModelCreatorAdapter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelEditorAdapter fallbackModelEditorAdapter() {
        return new FallbackModelEditorAdapter();
    }

}
