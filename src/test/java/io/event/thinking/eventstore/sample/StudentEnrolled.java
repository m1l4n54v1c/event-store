package io.event.thinking.eventstore.sample;

import io.event.thinking.eventstore.Event;

import static io.event.thinking.eventstore.Event.event;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.eventstore.sample.Constants.EVENT_TYPE;
import static io.event.thinking.eventstore.sample.Constants.STUDENT_ID;

record StudentEnrolled(String id, String firstName, String lastName) implements SerializableEvent {

    static final String NAME = "StudentEnrolled";

    @Override
    public Event toEvent() {
        return event(serialize(), tag(EVENT_TYPE, NAME), tag(STUDENT_ID, id));
    }

    static StudentEnrolled from(byte[] bytes) {
        return SerializableEvent.deserialize(bytes);
    }
}
