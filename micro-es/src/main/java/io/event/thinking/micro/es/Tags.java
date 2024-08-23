package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Tag;

import static io.event.thinking.eventstore.api.Tag.tag;

/**
 * Factory of {@link Tag}s.
 */
public class Tags {

    public static final String EVENT_TYPE = "eventType";

    private Tags() {
        // prevent instantiation
    }

    /**
     * Creates event type tag with {@link #EVENT_TYPE} as a key and given {@code type} as the value.
     *
     * @param type the type of the event
     * @return the tag
     */
    public static Tag type(String type) {
        return tag(EVENT_TYPE, type);
    }
}
