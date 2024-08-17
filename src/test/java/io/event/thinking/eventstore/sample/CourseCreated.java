package io.event.thinking.eventstore.sample;

import io.event.thinking.eventstore.Event;

import static io.event.thinking.eventstore.Event.event;
import static io.event.thinking.eventstore.Tag.tag;
import static io.event.thinking.eventstore.sample.Constants.COURSE_ID;
import static io.event.thinking.eventstore.sample.Constants.EVENT_TYPE;

record CourseCreated(String id, int capacity) implements SerializableEvent {

    static final String NAME = "CourseCreated";

    @Override
    public Event toEvent() {
        return event(serialize(), tag(EVENT_TYPE, NAME), tag(COURSE_ID, id));
    }

    static CourseCreated from(byte[] bytes) {
        return SerializableEvent.deserialize(bytes);
    }
}
