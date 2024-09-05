package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Index;

import static io.event.thinking.eventstore.api.Index.index;

/**
 * Factory of {@link Index}es.
 */
public class Indices {

    public static final String EVENT_TYPE = "eventType";

    private Indices() {
        // prevent instantiation
    }

    /**
     * Creates event type index with {@link #EVENT_TYPE} as a key and given {@code type} as the value.
     *
     * @param type the type of the event
     * @return the index
     */
    public static Index typeIndex(String type) {
        return index(EVENT_TYPE, type);
    }
}
