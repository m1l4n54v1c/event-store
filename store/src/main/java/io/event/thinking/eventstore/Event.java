package io.event.thinking.eventstore;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Event tagged with a {@link Set} of tags. In other words this event is associated with various concepts from the
 * domain (or even some technical/geospatial/etc. concepts).
 *
 * @param tags    associated concepts with this event
 * @param payload the payload of the event
 */
public record Event(Set<Tag> tags, byte[] payload) {

    /**
     * Factory method for {@link Event}.
     *
     * @param tags    associated concepts with this event
     * @param payload the payload of the event
     * @return newly created {@link Event}
     */
    public static Event event(Set<Tag> tags, byte[] payload) {
        return new Event(tags, payload);
    }

    /**
     * Factory method for {@link Event}.
     *
     * @param payload the payload of the event
     * @param tags    associated concepts with this event
     * @return newly created {@link Event}
     */
    public static Event event(byte[] payload, Tag... tags) {
        return new Event(Set.of(tags), payload);
    }

    /**
     * Factory method for {@link Event}.
     *
     * @param payload the payload of the event
     * @return newly created {@link Event}
     */
    public static Event event(byte[] payload) {
        return new Event(emptySet(), payload);
    }
}
