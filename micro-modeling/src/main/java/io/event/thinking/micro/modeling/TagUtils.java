package io.event.thinking.micro.modeling;

import io.event.thinking.eventstore.Tag;

import static io.event.thinking.eventstore.Tag.tag;

public class TagUtils {

    public static final String EVENT_TYPE = "eventType";

    public static Tag type(String type) {
        return tag(EVENT_TYPE, type);
    }

}
