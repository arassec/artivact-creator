package com.arassec.artivact.creator.ui.model;

import com.arassec.artivact.creator.core.model.ArtivactModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadModelUserData {

    private ArtivactModel model;

    private String filename;

}
