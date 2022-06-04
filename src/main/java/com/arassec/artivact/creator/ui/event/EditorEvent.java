package com.arassec.artivact.creator.ui.event;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@ToString
public class EditorEvent extends ApplicationEvent {

    public EditorEvent(EditorEventType type, int index) {
        super(type);
        this.type = type;
        this.index = index;
    }

    @Getter
    private final EditorEventType type;

    @Getter
    private final int index;

}
