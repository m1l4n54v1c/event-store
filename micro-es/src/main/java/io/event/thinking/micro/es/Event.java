package io.event.thinking.micro.es;

import io.event.thinking.eventstore.api.Tag;

import java.util.Set;

/**
 * In-memory representation of deserialized event.
 *
 * @param tags    associated concepts with this event
 * @param payload the payload of the event
 * @see io.event.thinking.eventstore.api.Event
 */
public record Event(Set<Tag> tags, Object payload) {

    /**
     * Creates this event with {@code tags} and {@code payload}.
     *
     * @param tags    associated concepts with this event
     * @param payload the payload of the event
     * @return the event
     */
    public static Event event(Set<Tag> tags, Object payload) {
        return new Event(tags, payload);
    }

    /**
     * Creates this event with {@code tags} and {@code payload}.
     *
     * @param payload the payload of the event
     * @param tags    associated concepts with this event
     * @return the event
     */
    public static Event event(Object payload, Tag... tags) {
        return new Event(Set.of(tags), payload);
    }
}
