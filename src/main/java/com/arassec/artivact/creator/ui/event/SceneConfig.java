package com.arassec.artivact.creator.ui.event;

import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SceneConfig {

    private String fxml;

    private Stage stage;

}
