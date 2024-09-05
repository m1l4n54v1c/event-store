package io.event.thinking.eventstore.api;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Event indexed with a {@link Set} of indices. In other words this event is associated with various concepts from the
 * domain (or even some technical/geospatial/etc. concepts).
 *
 * @param indices associated concepts with this event
 * @param payload the payload of the event
 */
public record Event(Set<Index> indices, byte[] payload) {

    /**
     * Factory method for {@link Event}.
     *
     * @param indices associated concepts with this event
     * @param payload the payload of the event
     * @return newly created {@link Event}
     */
    public static Event event(Set<Index> indices, byte[] payload) {
        return new Event(indices, payload);
    }

    /**
     * Factory method for {@link Event}.
     *
     * @param payload the payload of the event
     * @param indices associated concepts with this event
     * @return newly created {@link Event}
     */
    public static Event event(byte[] payload, Index... indices) {
        return new Event(Set.of(indices), payload);
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
