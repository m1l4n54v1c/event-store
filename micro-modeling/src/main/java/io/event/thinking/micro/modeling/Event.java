package io.event.thinking.micro.modeling;

import io.event.thinking.eventstore.api.Tag;

import java.util.Set;

public record Event(Set<Tag> tags, Object payload) {

    public static Event event(Set<Tag> tags, Object payload) {
        return new Event(tags, payload);
    }

    public static Event event(Object payload, Tag... tags) {
        return new Event(Set.of(tags), payload);
    }
}
