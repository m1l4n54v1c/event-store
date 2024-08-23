package io.event.thinking.micro.modeling;

import io.event.thinking.eventstore.api.Tag;

import static io.event.thinking.eventstore.api.Tag.tag;

public class Tags {

    public static final String EVENT_TYPE = "eventType";

    private Tags() {
        // prevent instantiation
    }

    public static Tag type(String type) {
        return tag(EVENT_TYPE, type);
    }
}
