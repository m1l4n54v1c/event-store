package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Index;

import java.util.Set;

/**
 * In-memory representation of deserialized event.
 *
 * @param indices associated concepts with this event
 * @param payload the payload of the event
 * @see io.event.thinking.eventstore.api.Event
 */
public record Event(Set<Index> indices, Object payload) {

    /**
     * Creates this event with {@code indices} and {@code payload}.
     *
     * @param indices    associated concepts with this event
     * @param payload the payload of the event
     * @return the event
     */
    public static Event event(Set<Index> indices, Object payload) {
        return new Event(indices, payload);
    }

    /**
     * Creates this event with {@code indices} and {@code payload}.
     *
     * @param payload the payload of the event
     * @param indices    associated concepts with this event
     * @return the event
     */
    public static Event event(Object payload, Index... indices) {
        return new Event(Set.of(indices), payload);
    }
}
